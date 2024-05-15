package com.stripe.android.utils

import com.stripe.android.model.Customer
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodUpdateParams
import com.stripe.android.paymentsheet.repositories.CustomerRepository

internal open class FakeCustomerRepository(
    private val paymentMethods: List<PaymentMethod> = emptyList(),
    private val customer: Customer? = null,
    private val onRetrieveCustomer: () -> Customer? = {
        customer
    },
    private val onGetPaymentMethods: () -> Result<List<PaymentMethod>> = {
        Result.success(paymentMethods)
    },
    private val onDetachPaymentMethod: (paymentMethodId: String) -> Result<PaymentMethod> = {
        Result.failure(NotImplementedError())
    },
    private val onAttachPaymentMethod: () -> Result<PaymentMethod> = {
        Result.failure(NotImplementedError())
    },
    private val onUpdatePaymentMethod: () -> Result<PaymentMethod> = {
        Result.failure(NotImplementedError())
    }
) : CustomerRepository {
    private val _detachRequests = mutableListOf<DetachRequest>()
    val detachRequests: List<DetachRequest> = _detachRequests

    var error: Throwable? = null

    override suspend fun retrieveCustomer(
        customerInfo: CustomerRepository.CustomerInfo
    ): Customer? = onRetrieveCustomer()

    override suspend fun getPaymentMethods(
        customerInfo: CustomerRepository.CustomerInfo,
        types: List<PaymentMethod.Type>,
        silentlyFail: Boolean,
    ): Result<List<PaymentMethod>> = onGetPaymentMethods()

    override suspend fun detachPaymentMethod(
        customerInfo: CustomerRepository.CustomerInfo,
        paymentMethodId: String
    ): Result<PaymentMethod> {
        _detachRequests.add(
            DetachRequest(
                paymentMethodId = paymentMethodId,
                customerInfo = customerInfo
            )
        )

        return onDetachPaymentMethod(paymentMethodId)
    }

    override suspend fun attachPaymentMethod(
        customerInfo: CustomerRepository.CustomerInfo,
        paymentMethodId: String
    ): Result<PaymentMethod> = onAttachPaymentMethod()

    override suspend fun updatePaymentMethod(
        customerInfo: CustomerRepository.CustomerInfo,
        paymentMethodId: String,
        params: PaymentMethodUpdateParams
    ): Result<PaymentMethod> = onUpdatePaymentMethod()

    data class DetachRequest(
        val paymentMethodId: String,
        val customerInfo: CustomerRepository.CustomerInfo,
    )
}
