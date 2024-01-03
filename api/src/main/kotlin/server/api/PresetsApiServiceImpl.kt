package server.api

import engine.context.Preset
import org.springframework.stereotype.Service
import server.models.PresetEntry
import server.models.SettingWithValue

@Service
class PresetsApiServiceImpl : PresetsApiService {
    override fun getPresets() =
        Preset.entries.map {
            PresetEntry(
                it.name,
                it.description,
                it.settings.map { (setting, value) -> SettingWithValue(setting.name, value.name) },
            )
        }
}
