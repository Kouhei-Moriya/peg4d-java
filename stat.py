import os, subprocess, math

TEST_DIR = "infer_test"
TMP_DIR = os.path.join(TEST_DIR, "tmp")
LOG_DIR = os.path.join(TEST_DIR, "log")

def frange(x, y, jump):
    while x < y:
        yield x
        x += jump

def tmpfile_path(path, num):
    if num == 0:
        return ""
    elif isinstance(num, float):
        return os.path.join(TMP_DIR, path + "-" + str(num) + ".small")
    else:
        return os.path.join(TMP_DIR, path + "-" + str(num) + ".big")

def generate_test_files():
    for path in os.listdir(TEST_DIR):
        full_path = os.path.join(TEST_DIR, path)
        if not os.path.isfile(full_path): continue
        if path.startswith("."): continue
        print(full_path)
        for i in range(1, 10):
            command = "cat " + tmpfile_path(path, i - 1) + " " + full_path + " > " + tmpfile_path(path, i)
            print(command)
            subprocess.call(command, shell=True)
        size_of_line = sum(1 for line in open(full_path))
        for f in frange(0.1, 1, 0.1):
            line_num = int(math.ceil(size_of_line * f))
            command = "head -n " + str(line_num) + " " + full_path + " > " + tmpfile_path(path, f)
            print(command)
            subprocess.call(command, shell=True)

def stat():
    for path in os.listdir(TEST_DIR):
        full_path = os.path.join(TEST_DIR, path)
        if not os.path.isfile(full_path): continue
        if path.startswith("."): continue
        #stat_for_speeds(path)
        stat_for_correctness(path)

def stat_for_speeds(target_file):
    command_base = "java -jar nez-0.9.3.jar -g sample/tokenize.p4d -l {log} -t {target}"
    for path in os.listdir(TMP_DIR):
        full_path = os.path.join(TMP_DIR, path)
        if not os.path.isfile(full_path): continue
        if path.startswith("."): continue
        if path.startswith(target_file) and path.endswith(".big"):
            command = command_base.format(log=os.path.join(LOG_DIR, path), target=full_path)
            print(command)
            subprocess.call(command, shell=True)

def stat_for_correctness(target_file):
    command_infer = "java -jar nez-0.9.3.jar -g sample/tokenize.p4d -t {target} -o tmp.p4d"
    command_check = "java -jar nez-0.9.3.jar -g tmp.p4d -t {target} -c"
    ret = target_file + ","
    for path in os.listdir(TMP_DIR):
        full_path = os.path.join(TMP_DIR, path)
        if not os.path.isfile(full_path): continue
        if path.startswith("."): continue
        if path.startswith(target_file) and path.endswith(".small"):
            command = command_infer.format(target=full_path)
            subprocess.call(command, shell=True)
            command = command_check.format(target=os.path.join(TEST_DIR, target_file))
            ret += subprocess.check_output(command, shell=True).replace("\n", "") + ","
    print(ret)

def combine():
    paths = []
    for path in os.listdir(LOG_DIR):
        full_path = os.path.join(LOG_DIR, path)
        if not os.path.isfile(full_path): continue
        paths.append(full_path)
    paths = sorted(paths)
    alllog = "./all.csv"
    subprocess.call("echo \"\" > " + alllog, shell=True)
    command_base = "cat {target} >> {alllog} && echo \"\" >> {alllog}"
    for path in paths:
        command = command_base.format(target=path, alllog=alllog)
        print(command)
        subprocess.call(command, shell=True)

def main():
    #generate_test_files()
    stat()
    #combine()

if __name__ == "__main__":
    main()
