#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include "svm-predict.h"
#include "svm.h"

namespace svmpredict {
 
    struct svm_node *x;
    int max_nr_attr = 64;
    struct svm_model* model;
    int predict_probability=0;

    double predict(double speed)
    {

	if((model=svm_load_model("/storage/emulated/0/libsvm/model"))==0)
	{
		debug("can't open model file %s\n","path");
		exit(1);
	}

	x = (struct svm_node *) malloc(max_nr_attr*sizeof(struct svm_node));
	if(predict_probability)
	{
		if(svm_check_probability_model(model)==0)
		{
			debug("Model does not support probabiliy estimates\n");
			exit(1);
		}
	}
	else
	{
		if(svm_check_probability_model(model)!=0)
			debug("Model supports probability estimates, but disabled in prediction.\n");
	}
	
        int svm_type=svm_get_svm_type(model);
        int nr_class=svm_get_nr_class(model);
        double *prob_estimates=NULL;
        int j;

        if(predict_probability)
        {
            if (svm_type==NU_SVR || svm_type==EPSILON_SVR)
                debug("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma=%g\n",svm_get_svr_probability(model));
        }

        int i = 0;
        double target_label, predict_label;
        char *idx, *val, *label, *endptr;
        int inst_max_index = -1;

        target_label = 0;
        x[i].index = 1;
        inst_max_index = x[i].index;
        x[i].value = speed;
        ++i;
        x[i].index = -1;
        predict_label = svm_predict(model,x);
	svm_free_and_destroy_model(&model);
        free(x);
        return predict_label;
    }

}
