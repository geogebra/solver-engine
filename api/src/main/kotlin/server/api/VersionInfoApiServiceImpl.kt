package server.api

import org.springframework.stereotype.Service
import server.models.VersionInfo

@Service
class VersionInfoApiServiceImpl : VersionInfoApiService {
    override fun getVersionInfo(): VersionInfo {
        val commitSha = System.getenv("SOLVER_COMMIT_SHA")
        return VersionInfo(commitSha)
    }
}
