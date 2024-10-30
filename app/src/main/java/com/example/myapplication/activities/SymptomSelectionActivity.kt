package com.example.myapplication.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.helperClasses.*
import java.time.LocalDate
import kotlin.properties.Delegates

class SymptomSelectionActivity : ComponentActivity() {

    private lateinit var selectedDay: LocalDate  // День, выбранный в календаре
    private var userId by Delegates.notNull<Long>()

    // CheckBox-ы настроения
    private lateinit var checkBoxHappy: CheckBox
    private lateinit var checkBoxSad: CheckBox
    private lateinit var checkBoxAnxious: CheckBox
    private lateinit var checkBoxIrritable: CheckBox
    private lateinit var checkBoxCalm: CheckBox

    // CheckBox-ы вагинальных выделений
    private lateinit var checkBoxNormal: CheckBox
    private lateinit var checkBoxAbnormal: CheckBox
    private lateinit var checkBoxItching: CheckBox
    private lateinit var checkBoxOdor: CheckBox
    private lateinit var checkBoxColorChange: CheckBox

    // RadioGroup и RadioButton-ы физической активности
    private lateinit var radioGroupPhysicalActivity: RadioGroup
    private lateinit var radioButtonActive: RadioButton
    private lateinit var radioButtonModerate: RadioButton
    private lateinit var radioButtonInactive: RadioButton
    private lateinit var radioButtonExhausted: RadioButton
    private lateinit var radioButtonEnergetic: RadioButton

    // CheckBox-ы боли в теле
    private lateinit var checkBoxHeadache: CheckBox
    private lateinit var checkBoxBackPain: CheckBox
    private lateinit var checkBoxAbdominalPain: CheckBox
    private lateinit var checkBoxJointPain: CheckBox
    private lateinit var checkBoxMusclePain: CheckBox

    // CheckBox-ы состояния кожи
    private lateinit var checkBoxAcne: CheckBox
    private lateinit var checkBoxDrySkin: CheckBox
    private lateinit var checkBoxOilySkin: CheckBox
    private lateinit var checkBoxRash: CheckBox
    private lateinit var checkBoxRedness: CheckBox

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_symptom_selection)

        // Получение выбранного дня из Intent
        selectedDay = LocalDate.parse(intent.getStringExtra("selected_day"))
        userId = intent.getLongExtra("user_id", -1)

        // Инициализация CheckBox-ов настроения
        checkBoxHappy = findViewById(R.id.checkBoxHappy)
        checkBoxSad = findViewById(R.id.checkBoxSad)
        checkBoxAnxious = findViewById(R.id.checkBoxAnxious)
        checkBoxIrritable = findViewById(R.id.checkBoxIrritable)
        checkBoxCalm = findViewById(R.id.checkBoxCalm)

        // Инициализация CheckBox-ов вагинальных выделений
        checkBoxNormal = findViewById(R.id.checkBoxNormal)
        checkBoxAbnormal = findViewById(R.id.checkBoxAbnormal)
        checkBoxItching = findViewById(R.id.checkBoxItching)
        checkBoxOdor = findViewById(R.id.checkBoxOdor)
        checkBoxColorChange = findViewById(R.id.checkBoxColorChange)

        // Инициализация RadioGroup и RadioButton-ов физической активности
        radioGroupPhysicalActivity = findViewById(R.id.radioGroupPhysicalActivity)
        radioButtonActive = findViewById(R.id.radioButtonActive)
        radioButtonModerate = findViewById(R.id.radioButtonModerate)
        radioButtonInactive = findViewById(R.id.radioButtonInactive)
        radioButtonExhausted = findViewById(R.id.radioButtonExhausted)
        radioButtonEnergetic = findViewById(R.id.radioButtonEnergetic)

        // Инициализация CheckBox-ов боли в теле
        checkBoxHeadache = findViewById(R.id.checkBoxHeadache)
        checkBoxBackPain = findViewById(R.id.checkBoxBackPain)
        checkBoxAbdominalPain = findViewById(R.id.checkBoxAbdominalPain)
        checkBoxJointPain = findViewById(R.id.checkBoxJointPain)
        checkBoxMusclePain = findViewById(R.id.checkBoxMusclePain)

        // Инициализация CheckBox-ов состояния кожи
        checkBoxAcne = findViewById(R.id.checkBoxAcne)
        checkBoxDrySkin = findViewById(R.id.checkBoxDrySkin)
        checkBoxOilySkin = findViewById(R.id.checkBoxOilySkin)
        checkBoxRash = findViewById(R.id.checkBoxRash)
        checkBoxRedness = findViewById(R.id.checkBoxRedness)

        // Загружаем ранее сохраненные симптомы
        loadSymptomsForDay(selectedDay)

        // Обработчик кнопки "Сохранить"
        val buttonSave = findViewById<Button>(R.id.buttonSaveSymptoms)
        buttonSave.setOnClickListener {
            // Сбор выбранных симптомов
            val selectedMoodSymptoms = mutableListOf<MoodSymptoms>()
            if (checkBoxHappy.isChecked) selectedMoodSymptoms.add(MoodSymptoms.HAPPY)
            if (checkBoxSad.isChecked) selectedMoodSymptoms.add(MoodSymptoms.SAD)
            if (checkBoxAnxious.isChecked) selectedMoodSymptoms.add(MoodSymptoms.ANXIOUS)
            if (checkBoxIrritable.isChecked) selectedMoodSymptoms.add(MoodSymptoms.IRRITABLE)
            if (checkBoxCalm.isChecked) selectedMoodSymptoms.add(MoodSymptoms.CALM)

            val selectedVaginalSymptoms = mutableListOf<VaginalDischargeSymptoms>()
            if (checkBoxNormal.isChecked) selectedVaginalSymptoms.add(VaginalDischargeSymptoms.NORMAL)
            if (checkBoxAbnormal.isChecked) selectedVaginalSymptoms.add(VaginalDischargeSymptoms.ABNORMAL)
            if (checkBoxItching.isChecked) selectedVaginalSymptoms.add(VaginalDischargeSymptoms.ITCHING)
            if (checkBoxOdor.isChecked) selectedVaginalSymptoms.add(VaginalDischargeSymptoms.ODOR)
            if (checkBoxColorChange.isChecked) selectedVaginalSymptoms.add(VaginalDischargeSymptoms.COLOR_CHANGE)

            // Получаем одно выбранное значение физической активности
            val selectedPhysicalActivity: PhysicalActivitySymptoms? = when (radioGroupPhysicalActivity.checkedRadioButtonId) {
                R.id.radioButtonActive -> PhysicalActivitySymptoms.ACTIVE
                R.id.radioButtonModerate -> PhysicalActivitySymptoms.MODERATE
                R.id.radioButtonInactive -> PhysicalActivitySymptoms.INACTIVE
                R.id.radioButtonExhausted -> PhysicalActivitySymptoms.EXHAUSTED
                R.id.radioButtonEnergetic -> PhysicalActivitySymptoms.ENERGETIC
                else -> null
            }

            val selectedBodyPainSymptoms = mutableListOf<BodyPainSymptoms>()
            if (checkBoxHeadache.isChecked) selectedBodyPainSymptoms.add(BodyPainSymptoms.HEADACHE)
            if (checkBoxBackPain.isChecked) selectedBodyPainSymptoms.add(BodyPainSymptoms.BACK_PAIN)
            if (checkBoxAbdominalPain.isChecked) selectedBodyPainSymptoms.add(BodyPainSymptoms.ABDOMINAL_PAIN)
            if (checkBoxJointPain.isChecked) selectedBodyPainSymptoms.add(BodyPainSymptoms.JOINT_PAIN)
            if (checkBoxMusclePain.isChecked) selectedBodyPainSymptoms.add(BodyPainSymptoms.MUSCLE_PAIN)

            val selectedSkinConditionSymptoms = mutableListOf<SkinConditionSymptoms>()
            if (checkBoxAcne.isChecked) selectedSkinConditionSymptoms.add(SkinConditionSymptoms.ACNE)
            if (checkBoxDrySkin.isChecked) selectedSkinConditionSymptoms.add(SkinConditionSymptoms.DRY_SKIN)
            if (checkBoxOilySkin.isChecked) selectedSkinConditionSymptoms.add(SkinConditionSymptoms.OILY_SKIN)
            if (checkBoxRash.isChecked) selectedSkinConditionSymptoms.add(SkinConditionSymptoms.RASH)
            if (checkBoxRedness.isChecked) selectedSkinConditionSymptoms.add(SkinConditionSymptoms.REDNESS)

            // Создание объекта Symptoms
            val symptoms = Symptoms(
                mood = selectedMoodSymptoms,
                vaginalDischarge = selectedVaginalSymptoms,
                physicalActivity = selectedPhysicalActivity,
                bodyPain = selectedBodyPainSymptoms,
                skinCondition = selectedSkinConditionSymptoms
            )

            // Сохраняем симптомы для выбранного дня
            saveSymptomsForDay(selectedDay, symptoms)

            // Уведомляем пользователя
            Toast.makeText(this, "Симптомы сохранены для $selectedDay", Toast.LENGTH_SHORT).show()

            // Закрываем экран
            finish()
        }
    }

    private fun loadSymptomsForDay(date: LocalDate) {
        val dbHelper = DatabaseHelper(this)
        val symptoms = dbHelper.getSymptoms(userId, date)

        symptoms?.let {
            // Установка значений для настроения
            checkBoxHappy.isChecked = it.mood.contains(MoodSymptoms.HAPPY)
            checkBoxSad.isChecked = it.mood.contains(MoodSymptoms.SAD)
            checkBoxAnxious.isChecked = it.mood.contains(MoodSymptoms.ANXIOUS)
            checkBoxIrritable.isChecked = it.mood.contains(MoodSymptoms.IRRITABLE)
            checkBoxCalm.isChecked = it.mood.contains(MoodSymptoms.CALM)

            // Установка значений для вагинальных выделений
            checkBoxNormal.isChecked = it.vaginalDischarge.contains(VaginalDischargeSymptoms.NORMAL)
            checkBoxAbnormal.isChecked = it.vaginalDischarge.contains(VaginalDischargeSymptoms.ABNORMAL)
            checkBoxItching.isChecked = it.vaginalDischarge.contains(VaginalDischargeSymptoms.ITCHING)
            checkBoxOdor.isChecked = it.vaginalDischarge.contains(VaginalDischargeSymptoms.ODOR)
            checkBoxColorChange.isChecked = it.vaginalDischarge.contains(VaginalDischargeSymptoms.COLOR_CHANGE)

            // Установка значений для физической активности
            when (it.physicalActivity) {
                PhysicalActivitySymptoms.ACTIVE -> radioGroupPhysicalActivity.check(R.id.radioButtonActive)
                PhysicalActivitySymptoms.MODERATE -> radioGroupPhysicalActivity.check(R.id.radioButtonModerate)
                PhysicalActivitySymptoms.INACTIVE -> radioGroupPhysicalActivity.check(R.id.radioButtonInactive)
                PhysicalActivitySymptoms.EXHAUSTED -> radioGroupPhysicalActivity.check(R.id.radioButtonExhausted)
                PhysicalActivitySymptoms.ENERGETIC -> radioGroupPhysicalActivity.check(R.id.radioButtonEnergetic)
                else -> radioGroupPhysicalActivity.clearCheck()
            }

            // Установка значений для боли в теле
            checkBoxHeadache.isChecked = it.bodyPain.contains(BodyPainSymptoms.HEADACHE)
            checkBoxBackPain.isChecked = it.bodyPain.contains(BodyPainSymptoms.BACK_PAIN)
            checkBoxAbdominalPain.isChecked = it.bodyPain.contains(BodyPainSymptoms.ABDOMINAL_PAIN)
            checkBoxJointPain.isChecked = it.bodyPain.contains(BodyPainSymptoms.JOINT_PAIN)
            checkBoxMusclePain.isChecked = it.bodyPain.contains(BodyPainSymptoms.MUSCLE_PAIN)

            // Установка значений для состояния кожи
            checkBoxAcne.isChecked = it.skinCondition.contains(SkinConditionSymptoms.ACNE)
            checkBoxDrySkin.isChecked = it.skinCondition.contains(SkinConditionSymptoms.DRY_SKIN)
            checkBoxOilySkin.isChecked = it.skinCondition.contains(SkinConditionSymptoms.OILY_SKIN)
            checkBoxRash.isChecked = it.skinCondition.contains(SkinConditionSymptoms.RASH)
            checkBoxRedness.isChecked = it.skinCondition.contains(SkinConditionSymptoms.REDNESS)
        }
    }


    private fun saveSymptomsForDay(date: LocalDate, symptoms: Symptoms) {
        val dbHelper = DatabaseHelper(this)
        val result = dbHelper.addSymptoms(userId, date, symptoms)
        if (result != -1L) {
            Log.d("Database", "Symptoms saved successfully with ID: $result")
        } else {
            Log.e("Database", "Failed to save symptoms")
            Toast.makeText(this, "Ошибка при сохранении симптомов", Toast.LENGTH_SHORT).show()
        }
    }
}
