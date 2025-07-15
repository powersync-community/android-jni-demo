#include "PowerSyncConnector.h"
#include "JNIEnv.h"
#include "jni.h"
#include "utils.h"
#include <android/log.h>
#include <map>

namespace PowerSync {

std::map<std::string, Connector> connector_map;

// Helper function to create a Java Credentials object from C++ struct
jobject createPowerSyncCredentials(JNIEnv *env, const std::string &endpoint,
                                   const std::string &token) {
  jclass credsClass =
      env->FindClass("com/powersync/connectors/PowerSyncCredentials");
  if (!credsClass)
    return nullptr;

  jmethodID ctor = env->GetMethodID(
      credsClass, "<init>",
      "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
  if (!ctor)
    return nullptr;

  jstring endpointStr = env->NewStringUTF(endpoint.c_str());
  jstring tokenStr = env->NewStringUTF(token.c_str());

  jobject credsObj = env->NewObject(credsClass, ctor, endpointStr, tokenStr,
                                    (jstring) nullptr);

  env->DeleteLocalRef(endpointStr);
  env->DeleteLocalRef(tokenStr);
  env->DeleteLocalRef(credsClass);

  return credsObj;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeConnector_fetchCredentialsC(
    JNIEnv *env, jobject thiz) {
  jclass cls = env->GetObjectClass(thiz);
  jfieldID fid = env->GetFieldID(cls, "identifier", "Ljava/lang/String;");
  auto identifier = (jstring)env->GetObjectField(thiz, fid);

  const char *identifierStr = env->GetStringUTFChars(identifier, nullptr);
  std::string id = std::string(identifierStr);

  env->ReleaseStringUTFChars(identifier, identifierStr);
  env->DeleteLocalRef(identifier);
  env->DeleteLocalRef(cls);

  auto connector_tuple = connector_map.find(id);
  if (connector_tuple == connector_map.end()) {
    throw std::runtime_error("Credential callback not found");
  }

  std::tuple<std::string, std::string> raw_credentials =
      connector_tuple->second.credentials_callback();

  jobject credentials = createPowerSyncCredentials(
      env, std::get<0>(raw_credentials), std::get<1>(raw_credentials));
  return credentials;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_powersync_demo_android_jni_wrapper_wrapper_uploadDataC(
    JNIEnv *env, jobject thiz, jstring op, jstring table, jstring data) {
  jclass cls = env->GetObjectClass(thiz);
  jfieldID fid = env->GetFieldID(cls, "identifier", "Ljava/lang/String;");
  auto identifier = (jstring)env->GetObjectField(thiz, fid);

  const char *identifierStr = env->GetStringUTFChars(identifier, nullptr);
  std::string id = std::string(identifierStr);

  env->ReleaseStringUTFChars(identifier, identifierStr);
  env->DeleteLocalRef(identifier);
  env->DeleteLocalRef(cls);

  auto connector_tuple = connector_map.find(id);
  if (connector_tuple == connector_map.end()) {
    throw std::runtime_error("Upload callback not found");
  }

  std::string opStr = env->GetStringUTFChars(op, nullptr);
  std::string tableStr = env->GetStringUTFChars(table, nullptr);
  std::string dataStr = env->GetStringUTFChars(data, nullptr);

  auto checkpoint =
      connector_tuple->second.upload_callback(opStr, tableStr, dataStr);

  jstring jcheckpoint = env->NewStringUTF(checkpoint.c_str());
  return jcheckpoint;
}

Connector::Connector(CredentialsCallback credentials_callback,
                     UploadCallback upload_callback)
    : credentials_callback(std::move(credentials_callback)),
      upload_callback(std::move(upload_callback)) {
  std::string id = create_random_id();
  JNIEnv *env = GetJniEnv();
  jclass native_connector_class =
      env->FindClass("com/powersync/demo/android/jni/wrapper/NativeConnector");
  jmethodID constructor = env->GetMethodID(native_connector_class, "<init>",
                                           "(Ljava/lang/String;)V");

  jstring connectorIdentifier = env->NewStringUTF(id.c_str());
  jobject connectorHandle =
      env->NewObject(native_connector_class, constructor, connectorIdentifier);
  handle = env->NewGlobalRef(connectorHandle);
  env->DeleteLocalRef(connectorIdentifier);
  connector_map.emplace(id, *this);
}

jobject Connector::getHandle() { return handle; }
} // namespace PowerSync