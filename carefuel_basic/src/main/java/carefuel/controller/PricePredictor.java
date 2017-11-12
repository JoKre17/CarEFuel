package carefuel.controller;

import org.tensorflow.*;

public class PricePredictor {
	public PricePredictor(){
		SavedModelBundle bundle =
				SavedModelBundle.load("/home/nils/Uni/InformatiCup/rnn_training/models/model", "serve");
		Session sess = bundle.session();

		Tensor prevMonths = Tensor.create(new float[50][744], Float.class);
		Tensor nPrevMonths = Tensor.create(new int[1], Integer.class);

		sess.runner().feed()

	}
}

// try to predict for two (2) sets of inputs.
Tensor inputs = new Tensor(tensorflow.DT_FLOAT, new TensorShape(2, 5));
FloatBuffer x = this.inputs.createBuffer();
x.put(new float[]{-6.0f,22.0f,383.0f,27.781754111198122f,-6.5f});
x.put(new float[]{66.0f,22.0f,2422.0f,45.72160947712418f,0.4f});
Tensor keepall = new Tensor(tensorflow.DT_FLOAT, new TensorShape(2,1));
((FloatBuffer)keepall.createBuffer()).put(new float[]{1f,1f});
TensorVector outputs = new TensorVector();
// to predict each time, pass in values for placeholders
outputs.resize(0);
s=session.Run(new StringTensorPairVector(new String[]{“Placeholder”,“Placeholder_2”},new Tensor[]{inputs,keepall}),new StringVector(“Sigmoid”),new StringVector(),outputs);if(!s.ok()){throw new RuntimeException(s.error_message().getString());}
// this is how you get back the predicted value from outputs
FloatBuffer output = this.outputs.get(0).createBuffer();for(
		int k = 0;k<output.limit();++k){
	System.out.println(“prediction=” + this.output.get(this.k));
}