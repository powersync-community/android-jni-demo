#include "PowerSyncSchema.h"
#include "JNIEnv.h"
#include <android/log.h>

namespace PowerSync {
Schema::Schema(const std::string &json) {
  JNIEnv *env = GetJniEnv();
  jclass clazz = env->FindClass("com/powersync/demo/android/jni/wrapper/NativeBridge");
  jmethodID createSchemaMethod = env->GetStaticMethodID(
      clazz, "createSchemaFromJson",
      "(Ljava/lang/String;)Lcom/powersync/db/schema/Schema;");

  jstring jsonString = env->NewStringUTF(json.c_str());

  jobject schemaHandle =
      env->CallStaticObjectMethod(clazz, createSchemaMethod, jsonString);

  schema = env->NewGlobalRef(schemaHandle);

  env->DeleteLocalRef(jsonString);
}

jobject Schema::getSchema() {
  JNIEnv *env = GetJniEnv();
  return env->NewLocalRef(schema);
}
} // namespace PowerSync