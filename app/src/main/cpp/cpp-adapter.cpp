#include "JNIEnv.h"
#include "PowerSyncConnector.h"
#include "PowerSyncDB.h"
#include "PowerSyncSchema.h"
#include "utils.h"
#include "secrets.h"
#include <android/log.h>
#include <jni.h>
#include <memory>
#include <string>
#include "OkHttp.h"

extern "C" JNIEXPORT void JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeBridge_init(JNIEnv *_, jclass _2) {
  // Not using the params of the function to simulate a pure C function that has
  // no access to the JNIEnv when called. Which might not strictly be true for
  // the vendors writing pure NDK apps but still a nice abstraction to allow for
  // "pure" C programs

  // 1. Create a schema based on JSON
  std::string schema_json = R"({
  "tables": [
      {
        "name": "todos",
        "columns": [
          { "name": "created_at", "type": "text" },
          { "name": "completed_at", "type": "text" },
          { "name": "description", "type": "text" },
          { "name": "created_by", "type": "text" },
          { "name": "completed", "type": "integer" },
          { "name": "list_id", "type": "text" },
          { "name": "photo_id", "type": "text" }
        ],
        "indexes": [
          { "name": "listid", "columns": [ { "name": "list_id" } ] }
        ]
      }
    ]
  })";

  auto schema = PowerSync::Schema(schema_json);

  // 2. Initialize the DB, currently not connected/syncing with anything
  auto powersync_db= std::make_shared<PowerSync::DB>(schema);

  // 3.a create a custom connector
  auto my_connector = PowerSync::Connector(
      []() -> std::tuple<std::string, std::string> {
        return {
            powersync::POWERSYNC_INSTANCE,
            powersync::POWERSYNC_TOKEN
        };
      },
      [](const std::string &op, const std::string &table,
         const std::string &data) -> std::string {
        __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB",
                            "🟩 upload callback with %s %s %s", op.c_str(),
                            table.c_str(), data.c_str());
        // Sample, the ids created should be consistent as checkpoints for the
        // reconciliation
        return PowerSync::create_random_id();
      });

  // 5. Finally create a status listener, to avoid doing operations before the
  // database is synced/is being synced with the listener attached then you can
  // finally connect and the syncing should start automatically
  powersync_db->register_status_listener([powersync_db](
                                             const std::string &status) {
    if (status.find("downloading=false") != std::string::npos &&
        status.find("connected=true") != std::string::npos) {

      std::vector<std::string> params;
      auto getAllRes = powersync_db->getAll("SELECT * FROM todos", params);

      __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB",
                          "🟩 getAll call res: %s", getAllRes.c_str());

      // Check if we already created our task from cpp
      if (getAllRes.find("my_cpp_task") != std::string::npos) {
        return;
      }

      std::vector<std::string> create_params;
      // Add id
      create_params.emplace_back("my_cpp_task");
      // Add description
      create_params.emplace_back("New todo item from C++");
      // Add created_by (user identifier)
      create_params.emplace_back("cpp-user");
      // Set completed to 0 (false)
      create_params.emplace_back("0");
      // Add a list_id
      create_params.emplace_back("375dac9d-a4d5-4db2-9a66-8ff2ac294756");
      // photo_id is optional, can be empty
      create_params.emplace_back("");

      powersync_db->execute("INSERT INTO todos ("
                            "id, description, created_by, completed, list_id, "
                            "photo_id) VALUES (?, ?, ?, ?, ?, ?)",
                            create_params);
    }
  });

  // 5 You can also use your custom connector
  powersync_db->connect(my_connector);

  // 6 extra do a https request
  //auto body = PowerSync::get("https://getip.info/");

//  __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB", "Get response %s", body.c_str());
}
