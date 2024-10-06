package com.example.myapplication.helperClasses

// Основной класс для хранения категорий симптомов
data class Symptoms(
    val mood: List<MoodSymptoms>,
    val vaginalDischarge: List<VaginalDischargeSymptoms>,
    val physicalActivity: List<PhysicalActivitySymptoms>,
    val bodyPain: List<BodyPainSymptoms>
)

// Enum для симптомов настроения
enum class MoodSymptoms(val description: String) {
    HAPPY("Счастливое настроение"),
    SAD("Грустное настроение"),
    ANXIOUS("Тревожное настроение"),
    IRRITABLE("Раздражительное настроение"),
    CALM("Спокойное настроение")
}

// Enum для симптомов вагинальных выделений
enum class VaginalDischargeSymptoms(val description: String) {
    NORMAL("Нормальные выделения"),
    ABNORMAL("Аномальные выделения"),
    ITCHING("Зуд"),
    ODOR("Неприятный запах"),
    COLOR_CHANGE("Изменение цвета")
}

// Enum для симптомов физической активности
enum class PhysicalActivitySymptoms(val description: String) {
    ACTIVE("Активная"),
    MODERATE("Умеренная"),
    INACTIVE("Неактивная"),
    EXHAUSTED("Истощенная"),
    ENERGETIC("Энергичная")
}

// Enum для симптомов боли в разных частях тела
enum class BodyPainSymptoms(val description: String) {
    HEADACHE("Головная боль"),
    BACK_PAIN("Боль в спине"),
    ABDOMINAL_PAIN("Боль в животе"),
    JOINT_PAIN("Боль в суставах"),
    MUSCLE_PAIN("Мышечная боль")
}