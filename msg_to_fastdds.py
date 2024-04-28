import subprocess
import sys
import tempfile
from pathlib import Path

from catkin_pkg.package import parse_package
from rosidl_adapter.msg import convert_msg_to_idl


def msg_to_idl(input_file: Path, output_dir: Path):
    input_file_ = input_file.resolve()
    output_dir_ = output_dir.resolve()
    package_dir = input_file_.parents[1]
    package = parse_package(package_dir)
    convert_msg_to_idl(
        package_dir,
        package.name,
        input_file_.relative_to(package_dir),
        output_dir_ / input_file_.parent.relative_to(input_file_.parents[2]),
    )


def idl_to_fastdds(input_dir: Path, output_dir: Path):
    input_dir_ = input_dir.resolve()
    output_dir_ = output_dir.resolve()
    for input_file in input_dir_.glob("**/*.idl"):
        subprocess.run(
            [
                "/workdir/Fast-DDS-Gen/scripts/fastddsgen",
                "-typeros2",
                "-I",
                input_dir_,
                "-d",
                output_dir_,
                "-cs",
                input_file,
            ],
            cwd=input_dir,
        )


with tempfile.TemporaryDirectory() as tmp_dir:
    tmp_dir_ = Path(tmp_dir)
    msg_to_idl(Path("rcl_interfaces/builtin_interfaces/msg/Time.msg"), tmp_dir_)
    msg_to_idl(Path("common_interfaces/std_msgs/msg/Header.msg"), tmp_dir_)
    msg_to_idl(Path("common_interfaces/sensor_msgs/msg/Joy.msg"), tmp_dir_)
    idl_to_fastdds(tmp_dir_, Path(sys.argv[1]))
