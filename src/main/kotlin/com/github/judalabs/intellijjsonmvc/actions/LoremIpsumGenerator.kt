package com.github.judalabs.intellijjsonmvc.actions

import com.intellij.psi.PsiMethod
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.*
import java.util.function.Supplier

enum class LoremIpsumGenerator(val loremIpsum: Supplier<String>) {

    UUID(Supplier { java.util.UUID.randomUUID().toString() }),
    STRING(Supplier { RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(10)) }),

    BOOLEAN(Supplier { RandomUtils.nextBoolean().toString() }),

    DATE(Supplier { Date().toString() }),
    LOCALDATETIME(Supplier { LocalDateTime.now().toString() }),
    LOCALDATE(Supplier { LocalDate.now().toString() }),
    YEARMONTH(Supplier { YearMonth.now().toString() }),

    LONG(Supplier { RandomUtils.nextInt().toString() }),
    INT(Supplier { RandomUtils.nextInt().toString() }),
    INTEGER(Supplier { RandomUtils.nextInt().toString() }),
    BIGINTEGER(Supplier { RandomUtils.nextInt().toString() }),

    FLOAT(Supplier { RandomUtils.nextDouble().toString() }),
    DOUBLE(Supplier { RandomUtils.nextDouble().toString() }),
    BIGDECIMAL(Supplier { RandomUtils.nextDouble().toString() });

    companion object {
        fun getLoremIpsum(method: PsiMethod): String {
            val fieldName = method.returnType?.presentableText?.uppercase()

            return try {
                valueOf(fieldName!!).loremIpsum.get()
            } catch (e: Exception) {
                "null"
            }
        }
    }
}