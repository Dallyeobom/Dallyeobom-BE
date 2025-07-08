package kr.dallyeobom.service

import kr.dallyeobom.client.KakaoApiClient
import kr.dallyeobom.controller.auth.request.KakaoLoginRequest
import kr.dallyeobom.controller.auth.request.KakaoUserCreateRequest
import kr.dallyeobom.controller.auth.request.TermsAgreeRequest
import kr.dallyeobom.controller.auth.response.KakaoLoginResponse
import kr.dallyeobom.controller.auth.response.NicknameCheckResponse
import kr.dallyeobom.controller.temporalAuth.request.CreateUserRequest
import kr.dallyeobom.controller.temporalAuth.response.ServiceTokensResponse
import kr.dallyeobom.controller.temporalAuth.response.TemporalUserResponse
import kr.dallyeobom.entity.Provder
import kr.dallyeobom.entity.Terms
import kr.dallyeobom.entity.TermsAgreeHistory
import kr.dallyeobom.entity.User
import kr.dallyeobom.entity.UserOauthInfo
import kr.dallyeobom.exception.AlreadyExistNicknameException
import kr.dallyeobom.exception.AlreadyExistedProviderUserIdException
import kr.dallyeobom.exception.InvalidRefreshTokenException
import kr.dallyeobom.exception.RecentTermsPolicyException
import kr.dallyeobom.exception.RequiredTermsAgreedPolicyException
import kr.dallyeobom.exception.TermsNotFoundException
import kr.dallyeobom.exception.UserNotFoundException
import kr.dallyeobom.repository.TermsAgreeHistoryRepository
import kr.dallyeobom.repository.TermsRepository
import kr.dallyeobom.repository.UserOauthInfoRepository
import kr.dallyeobom.repository.UserRepository
import kr.dallyeobom.util.jwt.JwtUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val kakaoApiClient: KakaoApiClient,
    private val userOauthInfoRepository: UserOauthInfoRepository,
    private val termsRepository: TermsRepository,
    private val termsAgreeHistoryRepository: TermsAgreeHistoryRepository,
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
    fun kakaoLogin(request: KakaoLoginRequest): KakaoLoginResponse {
        val kakaoProfile = kakaoApiClient.getKakaoProfile(request.providerAccessToken)
        val providerUserId = requireNotNull(kakaoProfile?.id) { "해당 계정 정보가 존재하지 않습니다." }
        val userOauthInfo = userOauthInfoRepository.findByProviderUserIdAndProvider(providerUserId, Provder.KAKAO)

        return userOauthInfo?.let {
            val tokens = makeTokens(it.user)
            KakaoLoginResponse(tokens.accessToken, tokens.refreshToken, isNewUser = false)
        } ?: KakaoLoginResponse(isNewUser = true)
    }

    @Transactional
    fun createUser(request: KakaoUserCreateRequest): ServiceTokensResponse {
        if (userRepository.existsByNickname(request.nickName)) {
            throw AlreadyExistNicknameException()
        }
        val kakaoProfile = kakaoApiClient.getKakaoProfile(request.providerAccessToken)
        val email = requireNotNull(kakaoProfile?.kakaoAccount?.email) { "이메일이 존재하지 않습니다." }
        val providerUserId = requireNotNull(kakaoProfile?.id) { "해당 계정 정보가 존재하지 않습니다." }

        if (userOauthInfoRepository.existsByProviderUserIdAndProvider(providerUserId, Provder.KAKAO)) {
            throw AlreadyExistedProviderUserIdException()
        }

        val user =
            userRepository.save(
                User(
                    nickname = request.nickName,
                    email = email,
                ),
            )
        userOauthInfoRepository.save(UserOauthInfo.createKakaoOauthInfo(user, providerUserId))
        validateAndSaveTermsAgreements(request, user.id)

        return makeTokens(user)
    }

    private fun validateAndSaveTermsAgreements(
        request: KakaoUserCreateRequest,
        userId: Long,
    ) {
        val termsRequestMap = request.terms.associateBy { it.termsType }
        val termsAgreeHistories =
            termsRepository
                .findAllByDeletedIsFalse()
                .map { terms ->
                    val submit = termsRequestMap[terms.type] ?: throw TermsNotFoundException(terms.type)
                    validateTermsPolicy(terms, submit)
                    TermsAgreeHistory(
                        userId = userId,
                        termsId = terms.id,
                        agreed = submit.agreed,
                    )
                }
        termsAgreeHistoryRepository.saveAll(termsAgreeHistories)
    }

    private fun validateTermsPolicy(
        terms: Terms,
        submit: TermsAgreeRequest,
    ) {
        if ((terms.required && !submit.agreed)) {
            throw RequiredTermsAgreedPolicyException()
        }
        if (submit.id != terms.id) {
            throw RecentTermsPolicyException()
        }
    }

    @Transactional(readOnly = true)
    fun checkDuplicatedNickName(nickname: String): NicknameCheckResponse = NicknameCheckResponse(userRepository.existsByNickname(nickname))

    @Deprecated("로그인 개발을 위한 provider 엑세스토큰 확인 API")
    @Transactional(readOnly = true)
    fun getProviderAccessToken(code: String) = kakaoApiClient.getToken(code)?.accessToken
}
