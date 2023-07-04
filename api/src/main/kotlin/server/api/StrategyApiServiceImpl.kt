package server.api

import methods.strategyRegistry
import org.springframework.stereotype.Service

@Service
class StrategyApiServiceImpl : StrategiesApiService {

    override fun getStrategies() = strategyRegistry.listStrategies()
}
