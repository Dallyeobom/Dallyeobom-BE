package kr.dallyeobom.repository

import kr.dallyeobom.entity.TermsAgreeHistory
import org.springframework.data.jpa.repository.JpaRepository

interface TermsAgreeHistoryRepository : JpaRepository<TermsAgreeHistory, Long>
