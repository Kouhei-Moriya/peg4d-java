import os, subprocess

TEST_DIR = "infer_test"
TMP_DIR = os.path.join(TEST_DIR, "tmp")
LOG_DIR = os.path.join(TEST_DIR, "log")

def tmpfile_path(path, num):
    if num == 0:
        return ""
    else:
        return os.path.join(TMP_DIR, path + "." + str(num))

def generate_test_files():
    for path in os.listdir(TEST_DIR):
        full_path = os.path.join(TEST_DIR, path)
        if not os.path.isfile(full_path): continue
        print(full_path)
        for i in range(1, 10):
            command = "cat " + tmpfile_path(path, i - 1) + " " + full_path + " > " + tmpfile_path(path, i)
            print(command)
            subprocess.call(command, shell=True)

def stat():
    command_base = "java -jar nez-0.9.3.jar -g sample/tokenize.p4d -l {log} -t {target}"
    for path in os.listdir(TMP_DIR):
        full_path = os.path.join(TMP_DIR, path)
        if not os.path.isfile(full_path): continue
        command = command_base.format(log=os.path.join(LOG_DIR, path), target=full_path)
        print(command)
        subprocess.call(command, shell=True)

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
    generate_test_files()
    stat()
    combine()

if __name__ == "__main__":
    main()
