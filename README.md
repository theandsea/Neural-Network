# CSE257_project
MNIST recognition by both neural network LeNet-5 and support vector machine with sequential minimal optimization

Only neural network to recognize MNIST is necessary but not sufficient and another project to recognize MNIST is need.
I choose support vector machine to do it.

I establish the neural network according this paper Gradient-Based Learning Applied to Document Recognition__lecun-98 YANN LECUN, MEMBER, IEEE, LEON BOTTOU, YOSHUA BENGIO, ´ AND PATRICK HAFFNER and reproduced LeNet-5 architecture.

I do the support vector machine to recognize MNIST by SMO and compare each pair of 2 groups of picture corresponding to 2 numbers and get a judge rule for each pair. In the general application situation, an unknown number was compared between different pair and when it is more likely to be one side, that side will get 1 score. In the end, the number that get the largest score will be considered as the recognize number.

The SVM will take a much longer time. In the program, I just set the scale of training data to be small and get an accuracy of 85%. You can change to a much bigger scale of training data if you want.

to use the MNIST data, we need to unzip 4 files in MNIST.rar to the root directory first before running the program.

run SVM_SMO.java to run SVM algorithm to train and recognize MNIST data

run neuralnetwork.java to run LeNet-5 architecture to train and recognize MNIST data
