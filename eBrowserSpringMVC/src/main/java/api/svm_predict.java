package api;

import libsvm.*;
import java.io.*;

class svm_predict {


	private static svm_print_interface svm_print_stdout = new svm_print_interface() {
		public void print(String s) {
			System.out.print(s);
		}
	};

	private static svm_print_interface svm_print_string = svm_print_stdout;

	static void info(String s) {
		svm_print_string.print(s);
	}

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static double predict(String speed, svm_model model) throws IOException {
		svm_node[] x = new svm_node[1];
		x[0] = new svm_node();
		x[0].index = atoi("1");
		x[0].value = atof(speed);
//		System.out.println(x[0].index + ":" + x[0].value);
		double v;
		v = svm.svm_predict(model, x);
		System.out.println(v);
		return v;
	}


	public static double main(String argv[]) throws IOException {
		svm_print_string = svm_print_stdout;
		svm_model model = svm.svm_load_model(argv[0]);
		if (model == null) {
			System.err.print("can't open model file " + argv[0] + "\n");
			System.exit(1);
		}
		return predict(argv[1], model);
	}
}
