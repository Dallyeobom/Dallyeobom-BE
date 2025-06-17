package kr.dallyeobom.service

import kr.dallyeobom.client.KakaoApiClient
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.controller.temporalAuth.request.CreateUserRequest
import kr.dallyeobom.controller.temporalAuth.response.ServiceTokensResponse
import kr.dallyeobom.controller.temporalAuth.response.TemporalUserResponse
import kr.dallyeobom.entity.User
import kr.dallyeobom.exception.AlreadyExistNicknameException
import kr.dallyeobom.exception.InvalidRefreshTokenException
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.util.jwt.JwtUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val kakaoApiClient: KakaoApiClient,
    private val jwtUtil: JwtUtil,
) {
    @Deprecated("정식로그인이 개발되기전 임시로 사용하는 메서드")
    fun getUsers(): List<TemporalUserResponse> = userRepository.findAll().map { TemporalUserResponse(it.id, it.nickname) }

    @Transactional
    @Deprecated("정식로그인이 개발되기전 임시로 사용하는 메서드")
    fun createUser(request: CreateUserRequest) {
        if (userRepository.existsByNickname(request.nickName)) {
            throw AlreadyExistNicknameException()
        }
        userRepository.save(User(request.nickName, ""))
    }

    @Deprecated("정식로그인이 개발되기전 임시로 사용하는 메서드")
    fun temporalLogin(userId: Long): ServiceTokensResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        return makeTokens(user)
    }

    fun refreshTokens(refreshToken: String): ServiceTokensResponse {
        require(
            jwtUtil.validateToken(jwtUtil.refreshKey, refreshToken) &&
                jwtUtil.validateCachedRefreshTokenRotateId(refreshToken),
        ) {
            throw InvalidRefreshTokenException()
        }
        val userId = jwtUtil.getUserId(jwtUtil.refreshKey, refreshToken)
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException(userId) }
        return makeTokens(user)
    }

    private fun makeTokens(user: User): ServiceTokensResponse {
        val accessToken = jwtUtil.generateAccessToken(user.id, user.nickname)
        val rotateId = jwtUtil.generateRotateId()
        val refreshToken = jwtUtil.generateRefreshToken(user.id, rotateId)
        jwtUtil.storeCachedRefreshTokenRotateId(user.id, rotateId)
        return ServiceTokensResponse(accessToken, refreshToken)
    }

    @Transactional(readOnly = true)
    fun kakaoLogin(code: String): KakaoLoginResponse {
        val token = kakaoApiClient.getToken(code)
        val kakaoProfile = token?.let { kakaoApiClient.getKakaoProfile(it.accessToken) }
        val email = requireNotNull(kakaoProfile?.kakaoAccount?.email) { "이메일이 존재하지 않습니다" }
        val user = userRepository.findByEmail(email)

        return when {
            user != null -> {
                val tokens = makeTokens(user)
                KakaoLoginResponse(tokens.accessToken, tokens.refreshToken, isNewUser = false)
            }
            else -> KakaoLoginResponse(isNewUser = true, email = kakaoProfile?.kakaoAccount?.email)
        }
    }

}
