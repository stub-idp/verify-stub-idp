package stubidp.kotlin.test.integration

import com.fasterxml.jackson.core.JsonProcessingException
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.assertj.core.api.Assertions
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import stubidp.saml.domain.assertions.Gender
import stubidp.stubidp.Urls
import stubidp.stubidp.builders.SimpleMdsValueBuilder
import stubidp.stubidp.builders.StubIdpBuilder
import stubidp.stubidp.domain.MatchingDatasetValue
import stubidp.stubidp.dtos.IdpUserDto
import stubidp.stubidp.security.BCryptHelper
import stubidp.stubidp.utils.TestUserCredentials
import stubidp.kotlin.test.integration.support.IntegrationTestHelper
import stubidp.kotlin.test.integration.support.StubIdpAppExtension
import stubidp.saml.domain.assertions.AuthnContext
import stubidp.utils.rest.common.HttpHeaders
import java.io.IOException
import java.text.MessageFormat
import java.time.Instant
import java.util.Arrays
import java.util.Optional
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

@ExtendWith(DropwizardExtensionsSupport::class)
class UserRepositoryIntegrationTests : IntegrationTestHelper() {
    protected var client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)

    @BeforeEach
    fun setUp() {
        val httpAuthenticationFeature = HttpAuthenticationFeature.basic(USERNAME, PASSWORD)
        client.register(httpAuthenticationFeature)
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldNotAllowIncorrectCredentialsTest() {
        val client: Client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false)
        val httpAuthenticationFeature = HttpAuthenticationFeature.basic("USERNAME", "PASSWORD")
        client.register(httpAuthenticationFeature)
        val user = UserBuilder.aUser().withUsername("user-11111").build()
        var response = client.target(getAddAllUsersPath(IDP_NAME))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getJson(java.util.List.of(user)), MediaType.APPLICATION_JSON_TYPE))
        Assertions.assertThat(response.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
        response = client.target(getDeleteUserPath(IDP_NAME))
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getJson(java.util.List.of(user)), MediaType.APPLICATION_JSON_TYPE))
        Assertions.assertThat(response.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    @Throws(Exception::class)
    fun addedUserShouldBePersistedTest() {
        val user = IdpUserDto(
                Optional.of("a pid"),
                "some test user",
                "some password",
                createOptionalMdsValue(Optional.of("some user firstname")),
                createOptionalMdsValue(Optional.of("some user middlename")), listOf(SimpleMdsValueBuilder.aSimpleMdsValue<String>().withValue("some user addSurname").build()),
                createOptionalMdsValue(Optional.of(Gender.FEMALE)),
                createOptionalMdsValue(Optional.of(Instant.now())),
                Optional.ofNullable(stubidp.saml.test.AddressBuilder.anAddress().withLines(Arrays.asList("blah", "blah2")).withPostCode("WC1V7AA").withVerified(true).build()),
                AuthnContext.LEVEL_4.toString()
        )
        aUserIsCreatedForIdp(IDP_NAME, user)
        val returnedUser = readEntity(aUserIsRequestedForIdp(user.username, IDP_NAME))
        Assertions.assertThat(returnedUser.pid).isEqualTo(user.pid)
        Assertions.assertThat(returnedUser.firstName.get().value).isEqualTo(user.firstName.get().value)
        Assertions.assertThat(returnedUser.firstName.get().isVerified).isEqualTo(user.firstName.get().isVerified)
        Assertions.assertThat(returnedUser.middleNames.get().value).isEqualTo(user.middleNames.get().value)
        Assertions.assertThat(returnedUser.middleNames.get().isVerified).isEqualTo(user.middleNames.get().isVerified)
        Assertions.assertThat(returnedUser.surnames[0].value).isEqualTo(user.surnames[0].value)
        Assertions.assertThat(returnedUser.surnames[0].isVerified).isEqualTo(user.surnames[0].isVerified)
        Assertions.assertThat(returnedUser.gender.get().value).isEqualTo(user.gender.get().value)
        Assertions.assertThat(returnedUser.gender.get().isVerified).isEqualTo(user.gender.get().isVerified)
        Assertions.assertThat(returnedUser.dateOfBirth.get().value).isEqualTo(user.dateOfBirth.get().value)
        Assertions.assertThat(returnedUser.dateOfBirth.get().isVerified).isEqualTo(user.dateOfBirth.get().isVerified)
        Assertions.assertThat(returnedUser.address.get().lines[0]).isEqualTo(user.address.get().lines[0])
        Assertions.assertThat(returnedUser.address.get().lines[1]).isEqualTo(user.address.get().lines[1])
        Assertions.assertThat(returnedUser.address.get().postCode).isEqualTo(user.address.get().postCode)
        Assertions.assertThat(returnedUser.address.get().isVerified).isEqualTo(user.address.get().isVerified)
        Assertions.assertThat(BCryptHelper.alreadyCrypted(returnedUser.password)).isTrue()
        Assertions.assertThat(returnedUser.levelOfAssurance).isEqualTo(user.levelOfAssurance)
    }

    @Test
    @Throws(Exception::class)
    fun allAddedUsersShouldBePersistedTest() {
        val user1 = UserBuilder.aUser().withUsername("user-1").build()
        val user2 = UserBuilder.aUser().withUsername("user-2").build()
        someUsersAreCreatedForIdp(IDP_NAME, user1, user2)
        val returnedUser1 = readEntity(aUserIsRequestedForIdp(user1.username, IDP_NAME))
        val returnedUser2 = readEntity(aUserIsRequestedForIdp(user2.username, IDP_NAME))
        Assertions.assertThat(returnedUser1.username).isEqualTo(user1.username)
        Assertions.assertThat(returnedUser2.username).isEqualTo(user2.username)
    }

    @Test
    @Throws(IOException::class)
    fun deletedUserShouldBeRemovedTest() {
        val deletableUser = UserBuilder.aUser().withUsername("deletable-user").build()
        someUsersAreCreatedForIdp(IDP_NAME, deletableUser)
        val returnedUser1 = readEntity(aUserIsRequestedForIdp(deletableUser.username, IDP_NAME))
        Assertions.assertThat(returnedUser1.username).isEqualTo(deletableUser.username)
        aUserIsDeletedFromIdp(IDP_NAME, deletableUser)
        val response = aUserIsRequestedForIdp(deletableUser.username, IDP_NAME)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @Throws(Exception::class)
    fun addedUserShouldHavePidGeneratedWhenNotSpecifiedTest() {
        val user = UserBuilder.aUser().withPid(null).build()
        aUserIsCreatedForIdp(IDP_NAME, user)
        val returnedUser = readEntity(aUserIsRequestedForIdp(user.username, IDP_NAME))
        Assertions.assertThat(returnedUser.pid.isPresent).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun userWithMissingLevelOfAssuranceShouldReturnBadRequestWithErrorMessage() {
        val user = UserBuilder.aUser().withLevelOfAssurance(null).build()
        val response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        Assertions.assertThat<String>(response.readEntity<List<String>>(object : GenericType<List<String>>() {})).containsOnly("Level of Assurance was not specified.")
    }

    @Test
    @Throws(Exception::class)
    fun userWithMissingUsernameShouldReturnBadRequestWithErrorMessage() {
        val user = UserBuilder.aUser().withUsername(null).build()
        val response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        Assertions.assertThat<String>(response.readEntity<List<String>>(object : GenericType<List<String>>() {})).containsOnly("Username was not specified or was empty.")
    }

    @Test
    @Throws(Exception::class)
    fun userWithMissingPasswordShouldReturnBadRequestWithErrorMessageTest() {
        val user = UserBuilder.aUser().withPassword(null).build()
        val response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user)
        Assertions.assertThat(response.status).isEqualTo(Response.Status.BAD_REQUEST.statusCode)
        Assertions.assertThat<String>(response.readEntity<List<String>>(object : GenericType<List<String>>() {})).containsOnly("Password was not specified or was empty.")
    }

    // convert object to json using apprule's object mapper because it can serialize guava correctly
    @Throws(JsonProcessingException::class)
    private fun getJson(o: Any): String {
        return applicationRule.objectMapper.writeValueAsString(o)
    }

    // convert object from json using apprule's object mapper because it can deserialize guava correctly
    @Throws(IOException::class)
    private fun readEntity(response: Response): IdpUserDto {
        // ensure data not stored by browser
        Assertions.assertThat(response.getHeaderString(HttpHeaders.CACHE_CONTROL_KEY)).isEqualTo(HttpHeaders.CACHE_CONTROL_NO_CACHE_VALUE)
        Assertions.assertThat(response.getHeaderString(HttpHeaders.PRAGMA_KEY)).isEqualTo(HttpHeaders.PRAGMA_NO_CACHE_VALUE)
        return applicationRule.objectMapper.readValue(response.readEntity(String::class.java), IdpUserDto::class.java)
    }

    class UserBuilder {
        private var levelOfAssurance: Optional<String> = Optional.ofNullable(AuthnContext.LEVEL_1.toString())
        private var username: Optional<String> = Optional.of("default-username")
        private val address = Optional.ofNullable(stubidp.saml.test.AddressBuilder.anAddress().withLines(Arrays.asList("line-1", "line-2")).build())
        private var password: Optional<String> = Optional.of("default-password")
        private var pid = Optional.of("default-pid")
        fun build(): IdpUserDto {
            return IdpUserDto(
                    pid,
                    username.orElse(null),
                    password.orElse(null),
                    Optional.empty(),
                    Optional.empty(), emptyList(),
                    Optional.empty(),
                    Optional.empty(),
                    address,
                    levelOfAssurance.orElse(null))
        }

        fun withLevelOfAssurance(levelOfAssurance: String?): UserBuilder {
            this.levelOfAssurance = Optional.ofNullable(levelOfAssurance)
            return this
        }

        fun withUsername(username: String?): UserBuilder {
            this.username = Optional.ofNullable(username)
            return this
        }

        fun withPassword(password: String?): UserBuilder {
            this.password = Optional.ofNullable(password)
            return this
        }

        fun withPid(pid: String?): UserBuilder {
            this.pid = Optional.ofNullable(pid)
            return this
        }

        companion object {
            fun aUser(): UserBuilder {
                return UserBuilder()
            }
        }
    }

    @Throws(JsonProcessingException::class)
    fun aUserIsCreatedForIdp(idpFriendlyId: String, user: IdpUserDto?) {
        val response = createARequest(getAddUserPath(idpFriendlyId)).post(Entity.entity(getJson(listOf(user)), MediaType.APPLICATION_JSON_TYPE))
        Assertions.assertThat(response.status).isEqualTo(201)
    }

    @Throws(JsonProcessingException::class)
    fun aUserIsCreatedForIdpWithoutResponseChecking(idpFriendlyId: String, user: IdpUserDto?): Response {
        return createARequest(getAddUserPath(idpFriendlyId)).post(Entity.entity(getJson(listOf(user)), MediaType.APPLICATION_JSON_TYPE))
    }

    @Throws(JsonProcessingException::class)
    fun someUsersAreCreatedForIdp(idpFriendlyId: String, vararg users: IdpUserDto?): Response {
        return createARequest(getAddAllUsersPath(idpFriendlyId)).post(Entity.entity(getJson(Arrays.asList(*users)), MediaType.APPLICATION_JSON_TYPE))
    }

    @Throws(JsonProcessingException::class)
    fun aUserIsDeletedFromIdp(idpFriendlyId: String, deletableUser: IdpUserDto) {
        val response = createARequest(getDeleteUserPath(idpFriendlyId)).post(Entity.entity(getJson(deletableUser), MediaType.APPLICATION_JSON_TYPE))
        Assertions.assertThat(response.status).isEqualTo(200)
    }

    fun aUserIsRequestedForIdp(username: String, idpFriendlyId: String): Response {
        return createARequest(getUserPath(username, idpFriendlyId)).get()
    }

    private fun createARequest(path: String): Invocation.Builder {
        return client.target(path).request().accept(MediaType.APPLICATION_JSON_TYPE)
    }

    private fun getAddUserPath(idpFriendlyId: String): String {
        return MessageFormat.format("{0}", getUserResourcePath(idpFriendlyId))
    }

    private fun getAddAllUsersPath(idpFriendlyId: String): String {
        return MessageFormat.format("{0}", getUserResourcePath(idpFriendlyId))
    }

    private fun getUserPath(username: String, idpFriendlyId: String): String {
        return MessageFormat.format("{0}{1}", getUserResourcePath(idpFriendlyId), UriBuilder.fromPath(Urls.GET_USER_PATH).build(username))
    }

    private fun getDeleteUserPath(idpFriendlyId: String): String {
        return MessageFormat.format("{0}{1}", getUserResourcePath(idpFriendlyId), UriBuilder.fromPath(Urls.DELETE_USER_PATH).build())
    }

    private fun getUserResourcePath(idpFriendlyId: String): String {
        return UriBuilder.fromPath("http://localhost:" + applicationRule.localPort + Urls.USERS_RESOURCE).build(idpFriendlyId).toASCIIString()
    }

    companion object {
        const val IDP_NAME = "user-repository-idp"
        const val DISPLAY_NAME = "User Repository Identity Service"
        private const val USERNAME = "integrationTestUser"
        private const val PASSWORD = "integrationTestUserPassword"
        val applicationRule = StubIdpAppExtension(java.util.Map.ofEntries<String, String>(java.util.Map.entry<String, String>("isIdpEnabled", "true"), java.util.Map.entry<String, String>("basicAuthEnabledForUserResource", "true")))
                .withStubIdp(StubIdpBuilder.aStubIdp()
                        .withId(IDP_NAME)
                        .withDisplayName(DISPLAY_NAME)
                        .addUserCredentials(TestUserCredentials(USERNAME, PASSWORD))
                        .build())

        private fun <T> createOptionalMdsValue(value: Optional<T>): Optional<MatchingDatasetValue<T>> {
            return value.map { t: T -> MatchingDatasetValue(t, null, null, true) }
        }
    }
}