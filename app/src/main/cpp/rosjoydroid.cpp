#include <ctime>
#include <string>

#include <jni.h>

#include <fastdds/dds/domain/DomainParticipant.hpp>
#include <fastdds/dds/domain/DomainParticipantFactory.hpp>
#include <fastdds/dds/publisher/DataWriter.hpp>
#include <fastdds/dds/publisher/Publisher.hpp>
#include <fastdds/dds/topic/TypeSupport.hpp>

#include "sensor_msgs/msg/JoyPubSubTypes.h"

namespace fastdds {
    using namespace eprosima::fastdds::dds;
    using namespace eprosima::fastrtps::rtps;
}

class JoyPublisher {
public:
    sensor_msgs::msg::Joy joy_msg;

    JoyPublisher(uint32_t domain_id, const std::string &ns) : type_(
            new sensor_msgs::msg::JoyPubSubType()) {
        std::string topic_name = "rt/" + ns + (ns.empty() ? "" : "/") + "joy";
        fastdds::TopicQos qos;
        qos.history().depth = 10;
        joy_msg.header().frame_id() = "joy";

        participant_ =
                fastdds::DomainParticipantFactory::get_instance()->create_participant(domain_id,
                                                                                      fastdds::PARTICIPANT_QOS_DEFAULT);
        type_.register_type(participant_);
        topic_ = participant_->create_topic(topic_name, type_.get_type_name(), qos);
        publisher_ = participant_->create_publisher(fastdds::PUBLISHER_QOS_DEFAULT);

        fastdds::DataWriterQos writer_qos = fastdds::DATAWRITER_QOS_DEFAULT;
        writer_qos.publish_mode().kind = fastdds::ASYNCHRONOUS_PUBLISH_MODE;
        writer_qos.endpoint().history_memory_policy = fastdds::PREALLOCATED_WITH_REALLOC_MEMORY_MODE;
        writer_qos.data_sharing().off();
        writer_ = publisher_->create_datawriter(topic_, writer_qos);
    }

    ~JoyPublisher() {
        publisher_->delete_datawriter(writer_);
        participant_->delete_publisher(publisher_);
        participant_->delete_topic(topic_);
        fastdds::DomainParticipantFactory::get_instance()->delete_participant(participant_);
    }

    void publish() {
        writer_->write(&joy_msg);
    }

private:
    fastdds::DomainParticipant *participant_;
    fastdds::Publisher *publisher_;
    fastdds::Topic *topic_;
    fastdds::DataWriter *writer_;
    fastdds::TypeSupport type_;
};

JoyPublisher *joy_publisher = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_jp_eyrin_rosjoydroid_MainActivity_createJoyPublisher(JNIEnv *env, jobject thiz, jint domain_id,
                                                          jstring ns) {
    const char *ns_ = env->GetStringUTFChars(ns, nullptr);
    joy_publisher = new JoyPublisher(domain_id, ns_);
    env->ReleaseStringUTFChars(ns, ns_);
}

extern "C"
JNIEXPORT void JNICALL
Java_jp_eyrin_rosjoydroid_MainActivity_destroyJoyPublisher(JNIEnv *env, jobject thiz) {
    delete joy_publisher;
    joy_publisher = nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_jp_eyrin_rosjoydroid_MainActivity_publishJoy(JNIEnv *env, jobject thiz, jfloatArray axes,
                                                  jintArray buttons) {
    timespec now;
    clock_gettime(CLOCK_REALTIME, &now);
    joy_publisher->joy_msg.header().stamp().sec() = now.tv_sec;
    joy_publisher->joy_msg.header().stamp().nanosec() = now.tv_nsec;

    joy_publisher->joy_msg.axes().resize(env->GetArrayLength(axes));
    joy_publisher->joy_msg.buttons().resize(env->GetArrayLength(buttons));
    env->GetFloatArrayRegion(axes, 0, joy_publisher->joy_msg.axes().size(),
                             joy_publisher->joy_msg.axes().data());
    env->GetIntArrayRegion(buttons, 0, joy_publisher->joy_msg.buttons().size(),
                           joy_publisher->joy_msg.buttons().data());

    joy_publisher->publish();
}
