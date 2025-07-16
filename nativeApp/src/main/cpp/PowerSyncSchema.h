#pragma once

#include <jni.h>
#include <string>

namespace PowerSync {

class Schema {
public:
  Schema(const std::string &json);
  jobject getSchema();

private:
  jobject schema;
};

} // namespace PowerSync