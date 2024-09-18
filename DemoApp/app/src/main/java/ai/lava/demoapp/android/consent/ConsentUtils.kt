package ai.lava.demoapp.android.consent

import ai.lava.demoapp.android.common.AppSession
import com.lava.lavasdk.ConsentListener
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaPIConsentFlag

object AppConsent {
    val defaultInitConsent = setOf("StrictlyNecessary")

    val defaultMapping: Map<String, Set<LavaPIConsentFlag>> = mapOf(
        "Strictly Necessary" to setOf(LavaPIConsentFlag.StrictlyNecessary),
        "Performance And Logging" to setOf(LavaPIConsentFlag.PerformanceAndLogging),
        "Functional" to setOf(LavaPIConsentFlag.Functional),
        "Targeting" to setOf(LavaPIConsentFlag.Targeting),
    )

    val customMapping: Map<String, Set<LavaPIConsentFlag>> = mapOf(
        "C0001" to setOf(LavaPIConsentFlag.StrictlyNecessary),
        "C0002" to setOf(LavaPIConsentFlag.PerformanceAndLogging),
        "C0003" to setOf(LavaPIConsentFlag.Functional),
        "C0004" to setOf(LavaPIConsentFlag.Targeting),
    )

    fun currentConsentMapping(): Map<String, Set<LavaPIConsentFlag>> {
        return if (AppSession.instance.getUseCustomConsent()) {
            customMapping
        } else {
            defaultMapping
        }
    }
}

object ConsentUtils {

    // Load the consent flags and store them in the AppSession
    fun getConsentFlags(): Set<LavaPIConsentFlag> {
        var consentFlags = AppSession.instance.getConsentFlags()

        if (consentFlags == null) {
            consentFlags = if (AppConsent.defaultInitConsent.isNullOrEmpty()) {
                AppConsent.defaultMapping.keys
            } else {
                AppConsent.defaultInitConsent
            }
        }

        AppSession.instance.setConsentFlags(consentFlags)
        AppSession.instance.setUseCustomConsent(false)

        return mapToLavaConsentFlags(
            consentFlags,
            AppConsent.currentConsentMapping()
        )
    }

    fun getCustomConsentFlags(
        predefined: Set<String>?
    ): Set<String> {
        var consentFlags = AppSession.instance.getConsentFlags()
        if (consentFlags == null) {
            consentFlags = if (predefined.isNullOrEmpty()) {
                AppConsent.customMapping.keys
            } else {
                predefined
            }
        }

        AppSession.instance.setConsentFlags(consentFlags)
        AppSession.instance.setUseCustomConsent(true)

        return consentFlags
    }


    fun mapToLavaConsentFlags(
        consentFlags: Set<String>,
        mapping: Map<String, Set<LavaPIConsentFlag>>
    ): Set<LavaPIConsentFlag> {
        return consentFlags.map {
            mapping[it] ?: setOf()
        }.flatten().toSet()
    }

    fun fromLavaConsentFlags(
        consentFlags: Set<LavaPIConsentFlag>,
        mapping: Map<String, Set<LavaPIConsentFlag>>
    ): Set<String> {
        return mapping.map { (key, value) ->
            if (consentFlags.containsAll(value)) {
                setOf(key)
            } else {
                emptySet()
            }
        }.flatten().toSet()
    }

    fun applyConsentFlags(
        consentFlags: Set<String>,
        listener: ConsentListener?
    ) {
        if (AppSession.instance.getUseCustomConsent()) {
            Lava.instance.setCustomPIConsentFlags(
                consentFlags,
                listener
            )
            return
        }

        val itemsToUpdate = mapToLavaConsentFlags(consentFlags, AppConsent.currentConsentMapping())
        Lava.instance.setPIConsentFlags(
            itemsToUpdate,
            listener
        )
    }

}