package kr.dallyeobom.service

import kr.dallyeobom.entity.User
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(userIdStr: String): UserDetails {
        val userId = userIdStr.toLong()
        return SecurityUser(userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) })
    }
}

class SecurityUser(
    val user: User,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf()

    override fun getPassword(): String = ""

    override fun getUsername(): String = ""

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
