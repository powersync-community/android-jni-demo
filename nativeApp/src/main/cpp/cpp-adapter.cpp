#include "JNIEnv.h"
#include "PowerSyncConnector.h"
#include "PowerSyncDB.h"
#include "PowerSyncSchema.h"
#include "utils.h"
#include <android/log.h>
#include <jni.h>
#include <memory>
#include <string>
#include <json.hpp>
#include "OkHttp.h"

static std::shared_ptr<PowerSync::DB> powersync_db;

extern "C" JNIEXPORT void JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeBridge_init(JNIEnv *env, jclass _2) {

using json = nlohmann::json;

  // 1. Create a schema based on JSON
  std::string schema_json = R"({
  "tables": [
    {
        "name": "lists",
        "columns": [
          { "name": "created_at", "type": "text" },
          { "name": "name", "type": "text" },
          { "name": "owner_id", "type": "text" }
        ]
      },
      {
        "name": "todos",
        "columns": [
          { "name": "created_at", "type": "text" },
          { "name": "completed_at", "type": "text" },
          { "name": "description", "type": "text" },
          { "name": "created_by", "type": "text" },
          { "name": "completed", "type": "integer" },
          { "name": "list_id", "type": "text" }
        ],
        "indexes": [
          { "name": "listid", "columns": [ { "name": "list_id" } ] }
        ]
      }
    ]
  })";

  auto schema = PowerSync::Schema(schema_json);

  // 2. Initialize the DB, initially not connected/syncing with anything
  powersync_db= std::make_shared<PowerSync::DB>(schema);

  // 3.a create a custom connector
  auto my_connector = PowerSync::Connector(
      []() -> std::tuple<std::string, std::string> {
        auto response = PowerSync::get("http://10.0.2.2:6060/api/auth/token");
        json data = json::parse(response);

        return {
            "http://10.0.2.2:8080",
            data["token"]
        };
      },
      [](const std::string &id, const std::string &op, const std::string &table,
         const std::string &data) -> std::optional<std::string> {
        __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB",
                            "🟩 upload callback with %s %s %s %s", id.c_str(), op.c_str(),
                            table.c_str(), data.c_str());

        json request = json::object();
        json jsonOp = json::object();
        jsonOp["table"] = table;
        jsonOp["op"] = op;
        jsonOp["id"] = id;
        jsonOp["data"] = json::parse(data);
        request["batch"] = json::array({jsonOp});

        PowerSync::postJson("http://10.0.2.2:6060/api/data", request.dump());
        return std::nullopt;
      });

  // 5. Finally create a status listener, to avoid doing operations before the
  // database is synced/is being synced with the listener attached then you can
  // finally connect and the syncing should start automatically
  powersync_db->register_status_listener([](
                                             const std::string &status) {
    __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB",
    "🟩 updated status: %s", status.c_str());
  });

  powersync_db->register_table_listener([env]() {
    auto getAllRes = powersync_db->getAll("SELECT * FROM todos", {});
    __android_log_print(ANDROID_LOG_INFO, "PowerSyncDB",
    "🟩 getAll call res: %s", getAllRes.c_str());

    auto bridge = env->FindClass("com/powersync/demo/android/jni/wrapper/NativeBridge");
    auto set_items = env->GetStaticMethodID(bridge, "setTodoItemsResults", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(bridge, set_items, env->NewStringUTF(getAllRes.c_str()));
  });

  // 5 You can use your custom connector
  powersync_db->connect(my_connector);
}

extern "C" JNIEXPORT void JNICALL
Java_com_powersync_demo_android_jni_wrapper_NativeBridge_createList(JNIEnv *env, jclass _2, jstring name) {
    const char* nativeName = env->GetStringUTFChars(name, nullptr);

    std::vector<std::string> create_params;
    create_params.emplace_back(nativeName);

    powersync_db->execute("INSERT INTO lists ("
    "id, created_at, name, owner_id) "
    "VALUES (uuid(), datetime(), ?, uuid())",
    create_params);

    env->ReleaseStringUTFChars(name, nativeName);
}
