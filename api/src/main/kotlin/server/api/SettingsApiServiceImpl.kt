package server.api

import engine.context.Setting
import org.springframework.stereotype.Service
import server.models.SettingEntry

@Service
class SettingsApiServiceImpl : SettingsApiService {
    override fun getSettings() =
        Setting.entries.map {
            SettingEntry(it.name, it.description, it.kind.settingValues.map { value -> value.name })
        }
}
