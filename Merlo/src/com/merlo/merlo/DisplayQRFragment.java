package com.merlo.merlo;

import java.io.FileNotFoundException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayQRFragment extends DialogFragment {

	ImageView qrImage;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		qrImage = new ImageView(getActivity());
		
		try {
			qrImage.setImageBitmap(BitmapFactory
					.decodeStream(getActivity().openFileInput("QRCODE.png")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		builder.setView(qrImage)
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		// Create the AlertDialog object and return it
			
		AlertDialog dialog = builder.create();
		
		return dialog;
	}
	
}
