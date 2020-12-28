import numpy as np
import sys


def HGD(D, R):
    len_D = len(D)
    len_R = len(R)
    y = int(len_R / 2)
    x = np.random.hypergeometric(len_D, len_R - len_D, y, 1)
    a = x[0]
    # 如果没有拿到样本，返回1
    if a == 0:
        return 1
    else:
        return a


if __name__ == '__main__':
    args = []
    for i in range(1, len(sys.argv)):
        args.append((int(sys.argv[i])))

    D = range(1, args[0] + 1)
    R = range(1, args[1] + 1)
    result = HGD(D, R)
    print(result)
