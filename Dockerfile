FROM ubuntu:22.04

ARG FASTDDS_GEN_VERSION=v3.3.0
ARG ROS_DISTRO=humble

WORKDIR /workdir

RUN apt update && \
    DEBIAN_FRONTEND=noninteractive apt install -y \
    git cpp openjdk-17-jdk python3 python3-empy python3-catkin-pkg

# Fast-DDS-Gen
RUN git clone -b $FASTDDS_GEN_VERSION --recursive https://github.com/eProsima/Fast-DDS-Gen.git && \
    cd Fast-DDS-Gen && \
    ./gradlew assemble

# rosidl_adapter
RUN git clone -b $ROS_DISTRO https://github.com/ros2/rosidl.git && \
    cp -R rosidl/rosidl_adapter/rosidl_adapter .

# interfaces
RUN git clone -b $ROS_DISTRO https://github.com/ros2/rcl_interfaces.git && \
    git clone -b $ROS_DISTRO https://github.com/ros2/common_interfaces.git

COPY msg_to_fastdds.py .

ENTRYPOINT ["python3", "msg_to_fastdds.py"]
