package com.example.myapplication.helperClasses

// Основной класс для хранения категорий симптомов
class Symptoms(
    val mood: List<MoodSymptoms>,                     // Список симптомов настроения
    val vaginalDischarge: List<VaginalDischargeSymptoms>, // Список симптомов выделений
    val physicalActivity: PhysicalActivitySymptoms?,    // Единственный симптом физической активности
    val bodyPain: List<BodyPainSymptoms>,               // Список симптомов боли
    val skinCondition: List<SkinConditionSymptoms>      // Список симптомов состояния кожи
) {
    override fun toString(): String {
        return "Symptoms(mood=$mood, vaginalDischarge=$vaginalDischarge, physicalActivity=$physicalActivity, bodyPain=$bodyPain, skinCondition=$skinCondition)"
    }
}


// Enum-like class for MoodSymptoms
class MoodSymptoms private constructor(val description: String) {

    companion object {
        val HAPPY = MoodSymptoms("Счастливое настроение")
        val SAD = MoodSymptoms("Грустное настроение")
        val ANXIOUS = MoodSymptoms("Тревожное настроение")
        val IRRITABLE = MoodSymptoms("Раздражительное настроение")
        val CALM = MoodSymptoms("Спокойное настроение")

        // valueOf method to convert a string into a MoodSymptoms object
        fun valueOf(description: String): MoodSymptoms? {
            return when (description) {
                HAPPY.description -> HAPPY
                SAD.description -> SAD
                ANXIOUS.description -> ANXIOUS
                IRRITABLE.description -> IRRITABLE
                CALM.description -> CALM
                else -> null // Возвращаем null, если симптом не найден
            }
        }
    }

    override fun toString(): String {
        return description
    }
}

// Enum-like class for VaginalDischargeSymptoms
class VaginalDischargeSymptoms private constructor(val description: String) {

    companion object {
        val NORMAL = VaginalDischargeSymptoms("Нормальные выделения")
        val ABNORMAL = VaginalDischargeSymptoms("Аномальные выделения")
        val ITCHING = VaginalDischargeSymptoms("Зуд")
        val ODOR = VaginalDischargeSymptoms("Неприятный запах")
        val COLOR_CHANGE = VaginalDischargeSymptoms("Изменение цвета")

        // valueOf method to convert a string into a VaginalDischargeSymptoms object
        fun valueOf(description: String): VaginalDischargeSymptoms? {
            return when (description) {
                NORMAL.description -> NORMAL
                ABNORMAL.description -> ABNORMAL
                ITCHING.description -> ITCHING
                ODOR.description -> ODOR
                COLOR_CHANGE.description -> COLOR_CHANGE
                else -> null
            }
        }
    }

    override fun toString(): String {
        return description
    }
}

// Enum-like class for PhysicalActivitySymptoms
class PhysicalActivitySymptoms private constructor(val description: String) {

    companion object {
        val ACTIVE = PhysicalActivitySymptoms("Активная")
        val MODERATE = PhysicalActivitySymptoms("Умеренная")
        val INACTIVE = PhysicalActivitySymptoms("Неактивная")
        val EXHAUSTED = PhysicalActivitySymptoms("Истощенная")
        val ENERGETIC = PhysicalActivitySymptoms("Энергичная")

        // valueOf method to convert a string into a PhysicalActivitySymptoms object
        fun valueOf(description: String): PhysicalActivitySymptoms? {
            return when (description) {
                ACTIVE.description -> ACTIVE
                MODERATE.description -> MODERATE
                INACTIVE.description -> INACTIVE
                EXHAUSTED.description -> EXHAUSTED
                ENERGETIC.description -> ENERGETIC
                else -> null
            }
        }
    }

    override fun toString(): String {
        return description
    }
}

// Enum-like class for BodyPainSymptoms
class BodyPainSymptoms private constructor(val description: String) {

    companion object {
        val HEADACHE = BodyPainSymptoms("Головная боль")
        val BACK_PAIN = BodyPainSymptoms("Боль в спине")
        val ABDOMINAL_PAIN = BodyPainSymptoms("Боль в животе")
        val JOINT_PAIN = BodyPainSymptoms("Боль в суставах")
        val MUSCLE_PAIN = BodyPainSymptoms("Мышечная боль")

        // valueOf method to convert a string into a BodyPainSymptoms object
        fun valueOf(description: String): BodyPainSymptoms? {
            return when (description) {
                HEADACHE.description -> HEADACHE
                BACK_PAIN.description -> BACK_PAIN
                ABDOMINAL_PAIN.description -> ABDOMINAL_PAIN
                JOINT_PAIN.description -> JOINT_PAIN
                MUSCLE_PAIN.description -> MUSCLE_PAIN
                else -> null
            }
        }
    }

    override fun toString(): String {
        return description
    }
}

// Enum-like class for SkinConditionSymptoms
class SkinConditionSymptoms private constructor(val description: String) {

    companion object {
        val ACNE = SkinConditionSymptoms("Акне")
        val DRY_SKIN = SkinConditionSymptoms("Сухая кожа")
        val OILY_SKIN = SkinConditionSymptoms("Жирная кожа")
        val RASH = SkinConditionSymptoms("Сыпь")
        val REDNESS = SkinConditionSymptoms("Покраснение")

        // valueOf method to convert a string into a SkinConditionSymptoms object
        fun valueOf(description: String): SkinConditionSymptoms? {
            return when (description) {
                ACNE.description -> ACNE
                DRY_SKIN.description -> DRY_SKIN
                OILY_SKIN.description -> OILY_SKIN
                RASH.description -> RASH
                REDNESS.description -> REDNESS
                else -> null
            }
        }
    }

    override fun toString(): String {
        return description
    }
}