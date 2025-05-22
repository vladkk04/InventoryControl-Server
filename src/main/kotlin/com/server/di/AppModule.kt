package com.server.di

import FileRepositoryImpl
import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import com.mongodb.AwsCredential
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.database.DatabaseFactory
import com.server.features.email.EmailConfirmationService
import com.server.features.email.EmailConfirmationServiceImpl
import com.server.features.email.EmailTokenRepository
import com.server.features.email.EmailTokenRepositoryImpl
import com.server.features.emailSender.EmailSenderService
import com.server.features.emailSender.EmailSenderServiceImpl
import com.server.features.file.FileRepository
import com.server.features.organisation.OrganisationRepository
import com.server.features.organisation.OrganisationRepositoryImpl
import com.server.features.organisation.invite.OrganisationInvitationRepository
import com.server.features.organisation.invite.OrganisationInvitationRepositoryImpl
import com.server.features.organisation.order.OrderRepository
import com.server.features.organisation.order.OrderRepositoryImpl
import com.server.features.organisation.product.ProductRepository
import com.server.features.organisation.product.ProductRepositoryImpl
import com.server.features.organisation.productCategory.ProductCategoryRepository
import com.server.features.organisation.productCategory.ProductCategoryRepositoryImpl
import com.server.features.organisation.settings.OrganisationSettingsRepository
import com.server.features.organisation.settings.OrganisationSettingsRepositoryImpl
import com.server.features.organisation.user.OrganisationUserRepository
import com.server.features.organisation.user.OrganisationUserRepositoryImpl
import com.server.features.otp.EmailOtpRepository
import com.server.features.otp.EmailOtpRepositoryImpl
import com.server.features.resetPassword.ResetPasswordTokenRepository
import com.server.features.resetPassword.ResetPasswordTokenRepositoryImpl
import com.server.features.s3.S3Service
import com.server.features.s3.S3ServiceImpl
import com.server.features.security.hashing.HashingService
import com.server.features.security.hashing.HashingServiceImpl
import com.server.features.security.jwtToken.JwtTokenConfig
import com.server.features.security.jwtToken.JwtTokenService
import com.server.features.security.jwtToken.JwtTokenServiceImpl
import com.server.features.updateStockProduct.UpdateStockProductRepository
import com.server.features.updateStockProduct.UpdateStockProductRepositoryImpl
import com.server.features.user.UserRepository
import com.server.features.user.UserRepositoryImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.server.netty.*
import org.koin.dsl.module

fun appModule() = module {
    //DatabaseFactory
    single<MongoDatabase> {
        try {
            DatabaseFactory.database
        } catch (e: Exception) {
            throw IllegalStateException("Failed recipient initialize MongoDB database: ${e.message}", e)
        }
    }

    single<S3Client> {
        S3Client {
            region = "eu-north-1"
            credentialsProvider = StaticCredentialsProvider.invoke {
                accessKeyId = System.getenv("AWS_ACCESS_KEY")
                secretAccessKey = System.getenv("AWS_SECRET_KEY")
            }

        }
    }

    single<HttpClient> {
        HttpClient(CIO) {
            install(Logging)
        }
    }

    single<JwtTokenConfig> {
        val secretKey = System.getenv("SECRET_KEY")
            ?: throw IllegalStateException("SECRET_KEY environment variable is not set")

        JwtTokenConfig(
            issuer = "http://inventory-control.click/",
            audience = "users",
            expiresIn = 60 * 60 * 30,
            secretKey = secretKey
        )
    }


    //Security
    single<HashingService> { HashingServiceImpl() }
    single<JwtTokenService> { JwtTokenServiceImpl(get()) }

    //Services
    single<EmailConfirmationService> { EmailConfirmationServiceImpl(get(), get()) }
    single<EmailSenderService> { EmailSenderServiceImpl(get(), get()) }
    single<EmailOtpRepository> { EmailOtpRepositoryImpl(get()) }
    single<EmailTokenRepository> { EmailTokenRepositoryImpl(get()) }

    //Repositories
    single<OrganisationRepository> { OrganisationRepositoryImpl(get()) }
    single<OrganisationInvitationRepository> { OrganisationInvitationRepositoryImpl(get()) }
    single<OrganisationUserRepository> { OrganisationUserRepositoryImpl(get(), get()) }

    single<ResetPasswordTokenRepository> { ResetPasswordTokenRepositoryImpl(get()) }

    single<UserRepository> { UserRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<ProductCategoryRepository> { ProductCategoryRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get(), get()) }

    single<UpdateStockProductRepository> { UpdateStockProductRepositoryImpl(get(), get()) }
    single<OrganisationSettingsRepository> { OrganisationSettingsRepositoryImpl(get()) }

    single<FileRepository> { FileRepositoryImpl() }

    single<S3Service> { S3ServiceImpl(get()) }


}