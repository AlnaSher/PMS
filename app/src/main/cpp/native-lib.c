#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#define LOG_TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

typedef struct {
    char *username;
    char *password;
} User;

static User *users = NULL;
static int user_count = 0;

// Функция для добавления пользователя
JNIEXPORT jboolean JNICALL
Java_com_example_myapplication_helperClasses_NativeLib_addUser(JNIEnv *env, jobject obj, jstring username, jstring password) {
    const char *username_c = (*env)->GetStringUTFChars(env, username, NULL);
    const char *password_c = (*env)->GetStringUTFChars(env, password, NULL);

    if (username_c == NULL || password_c == NULL) {
        LOGE("Ошибка получения строки.");
        return JNI_FALSE;
    }

    // Проверка на уникальность имени пользователя
    for (int i = 0; i < user_count; i++) {
        if (strcmp(users[i].username, username_c) == 0) {
            LOGE("Пользователь с именем %s уже существует.", username_c);
            (*env)->ReleaseStringUTFChars(env, username, username_c);
            (*env)->ReleaseStringUTFChars(env, password, password_c);
            return JNI_FALSE;
        }
    }

    // Расширение массива пользователей
    User *temp = realloc(users, sizeof(User) * (user_count + 1));
    if (temp == NULL) {
        LOGE("Ошибка выделения памяти.");
        (*env)->ReleaseStringUTFChars(env, username, username_c);
        (*env)->ReleaseStringUTFChars(env, password, password_c);
        return JNI_FALSE;
    }
    users = temp;

    // Сохранение данных пользователя
    users[user_count].username = strdup(username_c);
    users[user_count].password = strdup(password_c);
    if (users[user_count].username == NULL || users[user_count].password == NULL) {
        LOGE("Ошибка выделения памяти для имени пользователя или пароля.");
        (*env)->ReleaseStringUTFChars(env, username, username_c);
        (*env)->ReleaseStringUTFChars(env, password, password_c);
        return JNI_FALSE;
    }
    user_count++;

    LOGI("Пользователь %s добавлен.", username_c);

    (*env)->ReleaseStringUTFChars(env, username, username_c);
    (*env)->ReleaseStringUTFChars(env, password, password_c);
    return JNI_TRUE;
}

// Функция для проверки пользователя
JNIEXPORT jboolean JNICALL
Java_com_example_myapplication_helperClasses_NativeLib_isUserValid(JNIEnv *env, jobject obj, jstring username, jstring password) {
    const char *username_c = (*env)->GetStringUTFChars(env, username, NULL);
    const char *password_c = (*env)->GetStringUTFChars(env, password, NULL);

    if (username_c == NULL || password_c == NULL) {
        LOGE("Ошибка получения строки.");
        return JNI_FALSE;
    }

    for (int i = 0; i < user_count; i++) {
        if (strcmp(users[i].username, username_c) == 0 && strcmp(users[i].password, password_c) == 0) {
            LOGI("Пользователь %s проверен.", username_c);
            (*env)->ReleaseStringUTFChars(env, username, username_c);
            (*env)->ReleaseStringUTFChars(env, password, password_c);
            return JNI_TRUE;
        }
    }

    LOGE("Не удалось проверить пользователя %s.", username_c);
    (*env)->ReleaseStringUTFChars(env, username, username_c);
    (*env)->ReleaseStringUTFChars(env, password, password_c);
    return JNI_FALSE;
}

// Функция для освобождения памяти
JNIEXPORT void JNICALL
Java_com_example_myapplication_helperClasses_NativeLib_freeUsers(JNIEnv *env, jobject obj) {
    for (int i = 0; i < user_count; i++) {
        free(users[i].username);
        free(users[i].password);
    }
    free(users);
    users = NULL;
    user_count = 0;
    LOGI("Память пользователей освобождена.");
}
