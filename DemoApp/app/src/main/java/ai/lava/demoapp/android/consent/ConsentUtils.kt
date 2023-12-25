package ai.lava.demoapp.android.consent

import ai.lava.demoapp.android.common.AppSession
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaPIConsentFlag

enum class ConsentFlag(val flag: String) {
    StrictlyNecessary("Strictly Necessary"),
    PerformanceAndLogging("Performance And Logging"),
    Functional("Functional"),
    Targeting("Targeting"),
}

object ConsentUtils {

    fun getStoredConsentFlags(): Set<LavaPIConsentFlag> {
        val consentFlags = AppSession.instance.getConsentFlags()
        return mapToLavaConsentFlags(consentFlags)
    }

    fun mapToLavaConsentFlags(consentFlags: Set<ConsentFlag>): Set<LavaPIConsentFlag> {
        return consentFlags.map {
            when (it) {
                ConsentFlag.PerformanceAndLogging -> LavaPIConsentFlag.PerformanceAndLogging
                ConsentFlag.Functional -> LavaPIConsentFlag.Functional
                ConsentFlag.Targeting -> LavaPIConsentFlag.Targeting
                ConsentFlag.StrictlyNecessary -> LavaPIConsentFlag.StrictlyNecessary
            }
        }.toSet()
    }

    fun applyConsentFlags(consentFlags: Set<ConsentFlag>) {
        val target = mapToLavaConsentFlags(consentFlags)
        Lava.instance.setPIConsentFlags(target)
    }

    fun parseLavaPIConsentFlags(input: List<String>): Set<LavaPIConsentFlag> {
        return input.map {
            LavaPIConsentFlag.valueOf(it)
        }.toSet()

    }
}