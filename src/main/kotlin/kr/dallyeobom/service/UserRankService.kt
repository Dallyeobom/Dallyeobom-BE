package kr.dallyeobom.service

import kr.dallyeobom.controller.userRanking.UserRankType
import kr.dallyeobom.controller.userRanking.response.UserRankingResponse
import kr.dallyeobom.dto.UserRank
import kr.dallyeobom.repository.CourseCompletionHistoryRepository
import kr.dallyeobom.repository.ObjectStorageRepository
import kr.dallyeobom.util.lock.RedisLock
import org.redisson.api.RClientSideCaching
import org.redisson.api.RScoredSortedSet
import org.redisson.api.RedissonClient
import org.redisson.api.options.ClientSideCachingOptions
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UserRankService(
    private val redissonClient: RedissonClient,
    private val courseCompletionHistoryRepository: CourseCompletionHistoryRepository,
    private val objectStorageRepository: ObjectStorageRepository,
) {
    private val rankSets: Map<UserRankType, RScoredSortedSet<UserRank>> =
        mapOf(
            UserRankType.WEEKLY to createRankSet(WEEKLY_RANKING_KEY),
            UserRankType.MONTHLY to createRankSet(MONTHLY_RANKING_KEY),
            UserRankType.YEARLY to createRankSet(YEARLY_RANKING_KEY),
        )

    private fun createRankSet(key: String): RScoredSortedSet<UserRank> {
        val opts: ClientSideCachingOptions = ClientSideCachingOptions.defaults()
        val csc: RClientSideCaching = redissonClient.getClientSideCaching(opts)
        return csc.getScoredSortedSet(key)
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정에 실행
    @RedisLock(
        prefix = LOCK_NAME,
        key = "",
        waitTime = 3, // 락을 얻기 위해 최대 3초 대기
        leaseTime = 300, // 락을 얻으면 300초 동안 유지
    )
    fun refreshRank() {
        setRankData(WEEKLY_RANKING_KEY, LocalDate.now().minusWeeks(1))
        // minusMonths(1) 는 28일,30일,31일 이중에 어떤 수치를 가질지 달마다 달라짐 그래서 고정된 30으로 사용
        setRankData(MONTHLY_RANKING_KEY, LocalDate.now().minusDays(30))
        setRankData(YEARLY_RANKING_KEY, LocalDate.now().minusYears(1))
    }

    private fun setRankData(
        targetKey: String,
        startDate: LocalDate,
    ) {
        val tmpSet: RScoredSortedSet<UserRank> = redissonClient.getScoredSortedSet(TMP_RANKING_KEY)
        tmpSet.clear()

        val data: List<UserRank> =
            courseCompletionHistoryRepository.getDateRangeUserRankings(startDate.atStartOfDay()).onEach {
                it.profileImage =
                    it.profileImage?.let { profileImage -> objectStorageRepository.getDownloadUrl(profileImage) }
            }
        if (data.isEmpty()) {
            redissonClient.keys.delete(targetKey)
        } else {
            tmpSet.addAll(data.associateWith { it.runningLength.toDouble() }) // ① 임시 키에 새 데이터 적재
            redissonClient.keys.rename(TMP_RANKING_KEY, targetKey) // RENAME 은 O(1) 원자 연산, 기존 키를 즉시 교체
        }
    }

    fun getUserRanking(
        userId: Long,
        type: UserRankType,
    ): UserRankingResponse {
        val rankSet = requireNotNull(rankSets[type]) { "존재하지 않는 랭킹 타입입니다($type)" }
        val currentUserRank =
            rankSet.find { it.userId == userId }?.let { userRank ->
                rankSet.revRank(userRank)?.let { rankIndex ->
                    UserRankingResponse.CurrentUserRank(
                        rank = rankIndex + 1,
                        runningLength = userRank.runningLength,
                        completeCourseCount = userRank.completeCourseCount,
                    )
                }
            }
        return UserRankingResponse(
            rankSet.reversed().toList(),
            currentUserRank,
        )
    }

    companion object {
        private const val RANKING_KET_PREFIX = "user:rank:zset"
        private const val WEEKLY_RANKING_KEY = "${RANKING_KET_PREFIX}:weekly"
        private const val MONTHLY_RANKING_KEY = "${RANKING_KET_PREFIX}:monthly"
        private const val YEARLY_RANKING_KEY = "${RANKING_KET_PREFIX}:yearly"
        private const val TMP_RANKING_KEY = "${RANKING_KET_PREFIX}:tmp"

        private const val LOCK_NAME = "user:rank:lock"
    }
}
