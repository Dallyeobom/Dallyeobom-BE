package kr.dallyeobom.repository

import kr.dallyeobom.entity.Terms
import org.springframework.data.jpa.repository.JpaRepository

interface TermsRepository : JpaRepository<Terms, Long>
