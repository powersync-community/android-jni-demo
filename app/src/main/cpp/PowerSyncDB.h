#include "PowerSyncConnector.h"
#include "PowerSyncSchema.h"
#include <functional>
#include <jni.h>
#include <string>

namespace PowerSync {

class DB {
public:
  explicit DB(Schema schema);

  void execute(const std::string &query, const std::vector<std::string> &params);
  std::string getAll(const std::string &query, const std::vector<std::string> &params);
  void connect(Connector connector);
  void register_status_listener(
      std::function<void(const std::string &status)> status_callback);

private:
  std::string id;
  jobject db;
  jobject nativeDb;
};

} // namespace PowerSync