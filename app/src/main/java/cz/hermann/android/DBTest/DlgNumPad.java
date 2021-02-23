package cz.hermann.android.DBTest;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DlgNumPad extends AlertDialog 
{
    public interface OKListener {
        void onOK(double qty);
    }
	
// formatting helper	
    private static DecimalFormat qtyFormat = new DecimalFormat("#.##");//(DecimalFormat)DecimalFormat.getInstance();
    private static String decSep = ".";
    static
    {
		qtyFormat.setGroupingUsed(false);
		qtyFormat.setDecimalSeparatorAlwaysShown(false);
		decSep = Character.toString(qtyFormat.getDecimalFormatSymbols().getDecimalSeparator());
    }

	private Button btnDecSep = null;
	private TextView tvQty = null;
	private TextView tvName = null;
    private OKListener okListener = null;
    
	public double qty = 0;
	public String name = "";
	
	public DlgNumPad(Context context, OKListener listener)
	{
		this(context, listener, 2);
	}
	public DlgNumPad(Context context, OKListener listener, int decDigits) 
	{
		super(context, android.R.style.Theme_Dialog);
		okListener = listener;
		qtyFormat.setMaximumFractionDigits(decDigits);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.numpad);

		findViewById(R.id.btnClear).setOnClickListener(onBtnClick);
		findViewById(R.id.btnBack).setOnClickListener(onBtnClick);
		findViewById(R.id.btnEsc).setOnClickListener(onBtnClick);
		findViewById(R.id.btnEnter).setOnClickListener(onBtnClick);
		findViewById(R.id.btnDot).setOnClickListener(onBtnClick);
		findViewById(R.id.btn0).setOnClickListener(onBtnClick);
		findViewById(R.id.btn1).setOnClickListener(onBtnClick);
		findViewById(R.id.btn2).setOnClickListener(onBtnClick);
		findViewById(R.id.btn3).setOnClickListener(onBtnClick);
		findViewById(R.id.btn4).setOnClickListener(onBtnClick);
		findViewById(R.id.btn5).setOnClickListener(onBtnClick);
		findViewById(R.id.btn6).setOnClickListener(onBtnClick);
		findViewById(R.id.btn7).setOnClickListener(onBtnClick);
		findViewById(R.id.btn8).setOnClickListener(onBtnClick);
		findViewById(R.id.btn9).setOnClickListener(onBtnClick);
		
		btnDecSep = (Button)findViewById(R.id.btnDot);
		btnDecSep.setText(decSep);
		
		tvQty = (TextView)findViewById(R.id.tvQty);
		tvQty.setText(qtyFormat.format(qty));
		
		tvName = (TextView)findViewById(R.id.tvName);
		tvName.setText(name);
	}
	
	private View.OnClickListener onBtnClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			String strQty = tvQty.getText().toString();
			
			switch (v.getId()) 
			{
			case R.id.btnClear: tvQty.setText("0"); break;
			case R.id.btnBack:  
				if ( strQty.length() > 1 )
					strQty = strQty.substring(0, strQty.length()-1);
				else 
					strQty = "0";
				tvQty.setText(strQty);
				break;
			case R.id.btnEsc:   DlgNumPad.this.cancel(); break;
			case R.id.btnEnter:
				try
				{
					qty = qtyFormat.parse(strQty).doubleValue();
					if ( okListener != null )
						okListener.onOK(qty);
//					okListener = null;	// avoid callig the same listener twice - if e.g. when dialog is envoked repeatedly and no new listener is set
					dismiss();
				}
				catch(Exception ex)
				{
					GM.showError(DlgNumPad.this.getContext(), ex, "Enter number!");
				}
				break;
			case R.id.btnDot: 
				if ( !strQty.contains(decSep) ) 
					tvQty.append(decSep); 
				break;
			case R.id.btn0:
			case R.id.btn1:
			case R.id.btn2:
			case R.id.btn3:
			case R.id.btn4:
			case R.id.btn5:
			case R.id.btn6:
			case R.id.btn7:
			case R.id.btn8:
			case R.id.btn9:
				CharSequence val = ((Button)v).getText();
				if ( strQty.equals("0") ) 
					tvQty.setText(val);
				else if ( !strQty.contains(decSep) || strQty.length()-strQty.indexOf(decSep)<=qtyFormat.getMaximumFractionDigits() )
					tvQty.append(val);
				break;
			}
		};
	};
}
