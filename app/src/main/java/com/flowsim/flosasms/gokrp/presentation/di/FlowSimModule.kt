package com.flowsim.flosasms.gokrp.presentation.di

import com.flowsim.flosasms.gokrp.data.repo.FlowSimRepository
import com.flowsim.flosasms.gokrp.data.shar.FlowSimSharedPreference
import com.flowsim.flosasms.gokrp.data.utils.FlowSimPushToken
import com.flowsim.flosasms.gokrp.data.utils.FlowSimSystemService
import com.flowsim.flosasms.gokrp.domain.usecases.FlowSimGetAllUseCase
import com.flowsim.flosasms.gokrp.presentation.pushhandler.FlowSimPushHandler
import com.flowsim.flosasms.gokrp.presentation.ui.load.FlowSimLoadViewModel
import com.flowsim.flosasms.gokrp.presentation.ui.view.FlowSimViFun
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val flowSimModule = module {
    factory {
        FlowSimPushHandler()
    }
    single {
        FlowSimRepository()
    }
    single {
        FlowSimSharedPreference(get())
    }
    factory {
        FlowSimPushToken()
    }
    factory {
        FlowSimSystemService(get())
    }
    factory {
        FlowSimGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        FlowSimViFun(get())
    }
    viewModel {
        FlowSimLoadViewModel(get(), get(), get())
    }
}