#!/usr/bin/python3

import argparse

import queue as queue_package
import Communicators as communicators
import Threads as own_threads
import HAL

# Define here all supported sensors.
sensors = ["pressure"]
sensor = ["temperature", "location"]

parser = argparse.ArgumentParser()

parser.add_argument("-s", "--specific",
                  action="store_true", dest="test", default=False,
                  help="This flag enables test mode, now you can use only specific types of sensors.")

parser.add_argument("--rabbit",
                  action="store_true", dest="rabbit", default=False,
                  help="This flag causes json strings to send to rabbitMQ.")

parser.add_argument("--socket",
                  action="store_true", dest="socket", default=False,
                  help="This flag causes json strings to send by socket.")

for entry in sensors:
    parser.add_argument("--" + entry,
                      action="store_true", dest=entry, default=False,
                      help="This flag enables " + entry + " sensors in test mode")

for entry in sensor:
    parser.add_argument("--" + entry,
                      action="store_true", dest=entry, default=False,
                      help="This flag enables " + entry + " sensor in test mode")

args = parser.parse_args()

# get collect all needed sensor threads
json_queue = queue_package.Queue()
threads = []
if args.test:
    print("Test mode active!")

    if args.pressure:
        print("pressure activ!")
        threads.append(own_threads.SensorEvaluator(1, "SensorEvaluator_pressure", 1, 1, json_queue, HAL.serial_sensors))
    if args.temperature:
        print("temperature activ!")
        threads.append(own_threads.SensorEvaluator(2, "SensorEvaluator_temperature", 2,  30, json_queue, HAL.serial_sensors))
    if args.location:
        print("location activ!")
        threads.append(own_threads.SensorEvaluator(3, "SensorEvaluator_location", 3, 0, json_queue, HAL.serial_sensors))
else:
    threads.append(own_threads.SensorEvaluator(1, "SensorEvaluator_pressure", 1, 1, json_queue, HAL.serial_sensors))
    # threads.append(own_threads.SensorEvaluator(2, "SensorEvaluator_temperature", 2,  30, json_queue, HAL.serial_sensors))
    threads.append(own_threads.SensorEvaluator(3, "SensorEvaluator_location", 3, 0, json_queue, HAL.serial_sensors))

# get right communication medium
if args.rabbit:
    print("rabbit activ!")
    communicator = communicator = communicators.RabbitMQCommunicator("127.0.0.1", "sg.ex.sensor_values", "sg.rk.sensor_values", json_queue)
elif args.socket:
    print("socket activ!")
    communicator = communicators.SocketCommunicator("141.22.80.72", 15000)
else:
    communicator = communicators.CommunicatorDummy()

communicator.setup_connection()
threads.append(own_threads.MQ_Communicator(10, "MQ_Communicator", 10, json_queue, communicator))

# start the communicator thread
for thread in threads:
    thread.start()

# join all threads
for thread in threads:
    thread.join()
