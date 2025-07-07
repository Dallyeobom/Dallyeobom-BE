package kr.dallyeobom.repository

import kr.dallyeobom.entity.Terms
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TermsRepository : JpaRepository<Terms, Long> {
    @Query("SELECT t FROM Terms t WHERE t.deleted is false")
    fun findAllByDeletedIsFalse(): List<Terms>
}
