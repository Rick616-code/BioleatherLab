package pe.bioleather.movil;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    // 10.0.2.2 es el localhost del equipo anfitrión visto desde el emulador Android.
    private static final String BASE = "http://10.0.2.2:8080/api";
    private final java.util.concurrent.ExecutorService pool = Executors.newSingleThreadExecutor();
    private final int forest = Color.rgb(22,62,50), green = Color.rgb(23,107,85);
    private LinearLayout root, body;
    private String token = "";
    private long selectedId;
    private String selectedName;
    private int selectedPhoto;

    interface Done { void accept(JSONObject data) throws Exception; }
    private void request(String route, String method, JSONObject payload, Done done) {
        pool.execute(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection)new URL(BASE + route).openConnection();
                c.setRequestMethod(method); c.setConnectTimeout(7000); c.setReadTimeout(7000);
                c.setRequestProperty("Accept", "application/json");
                if (!token.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + token);
                if (payload != null) {
                    c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try (OutputStream out = c.getOutputStream()) { out.write(payload.toString().getBytes(StandardCharsets.UTF_8)); }
                }
                InputStream stream = c.getResponseCode()<400?c.getInputStream():c.getErrorStream();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096]; int length;
                while ((length=stream.read(buffer))!=-1) bytes.write(buffer,0,length);
                String response = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
                if (c.getResponseCode()>=400) throw new Exception(new JSONObject(response).optString("detail","Error HTTP "+c.getResponseCode()));
                JSONObject result = response.trim().startsWith("[") ? new JSONObject().put("items",new JSONArray(response)) : new JSONObject(response);
                runOnUiThread(() -> { try { done.accept(result); } catch(Exception ex) { fail(ex); } });
                c.disconnect();
            } catch(Exception ex) { runOnUiThread(() -> fail(ex)); }
        });
    }
    private void fail(Exception e) { Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show(); }
    @Override public void onCreate(Bundle b) { super.onCreate(b); home(); }
    private TextView text(String s,int size,boolean bold) {
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(17,26,22));
        if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(0,7,0,7);return t;
    }
    private Button button(String label,Runnable click) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));b.setOnClickListener(v->click.run());return b;
    }
    private EditText field(String hint,boolean password) {
        EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);
        e.setInputType(password?129:1);body.addView(e);return e;
    }
    private void frame(String heading) {
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.rgb(246,247,242));
        TextView brand=text("  B   Bioleather Lab",23,true);brand.setTextColor(Color.WHITE);brand.setBackgroundColor(forest);brand.setPadding(18,20,18,20);root.addView(brand);
        ScrollView scroll=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(22,22,22,22);scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);
        for(String tab:new String[]{"Inicio","Catálogo","Pedidos"}){
            Button n=button(tab,()->{if(tab.equals("Inicio"))home();else if(tab.equals("Catálogo"))catalog();else orders();});
            nav.addView(n,new LinearLayout.LayoutParams(0,56,1));
        }root.addView(nav);setContentView(root);
        body.addView(text(heading,31,true));
    }
    private void home() {
        frame("Accesorios sostenibles");body.addView(text("Carteras, billeteras y correas de diseño conceptual.",17,false));
        ImageView image=new ImageView(this);image.setImageResource(R.drawable.cartera);image.setScaleType(ImageView.ScaleType.CENTER_CROP);body.addView(image,new LinearLayout.LayoutParams(-1,250));
        body.addView(button("Explorar catálogo",this::catalog));
        body.addView(button(token.isEmpty()?"Ingresar o registrarme":"Mi sesión",this::auth));
        body.addView(text("Prototipo académico. Sin pagos ni confirmación de entrega.",13,false));
    }
    private void auth() {
        frame("Acceso y registro");EditText name=field("Nombre completo (para registro)",false), email=field("Correo electrónico",false), pass=field("Contraseña (mínimo 8 caracteres)",true);
        body.addView(button("Ingresar",()->{
            try { request("/auth/login","POST",new JSONObject().put("correo",email.getText().toString()).put("clave",pass.getText().toString()),data->{token=data.getString("token");Toast.makeText(this,"Sesión iniciada",Toast.LENGTH_SHORT).show();home();}); } catch(Exception e){fail(e);}
        }));
        body.addView(button("Crear cuenta",()->{
            try { request("/auth/register","POST",new JSONObject().put("nombre",name.getText().toString()).put("correo",email.getText().toString()).put("clave",pass.getText().toString()),data->Toast.makeText(this,"Cuenta creada. Ahora ingresa.",Toast.LENGTH_LONG).show()); } catch(Exception e){fail(e);}
        }));
    }
    private int photo(String image){if(image.equals("billetera.png"))return R.drawable.billetera;if(image.equals("correa.png"))return R.drawable.correa;return R.drawable.cartera;}
    private void catalog() {
        frame("Catálogo móvil");EditText search=field("Buscar por nombre",false);
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);body.addView(list);
        request("/products","GET",null,data->{JSONArray arr=data.getJSONArray("items");
            Runnable draw=()->{list.removeAllViews();for(int i=0;i<arr.length();i++){JSONObject p=arr.optJSONObject(i);if(!p.optString("nombre").toLowerCase().contains(search.getText().toString().toLowerCase()))continue;
                long id=p.optLong("id");String title=p.optString("nombre");int image=photo(p.optString("imagen"));
                ImageView img=new ImageView(this);img.setImageResource(image);img.setScaleType(ImageView.ScaleType.CENTER_CROP);list.addView(img,new LinearLayout.LayoutParams(-1,170));
                list.addView(text(title+" · S/ "+p.optString("precio"),21,true));list.addView(button("Ver detalle",()->detail(id,title,image,p.optString("descripcion"))));
            }};draw.run();search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){draw.run();}public void afterTextChanged(android.text.Editable e){}});
        });
    }
    private void detail(long id,String name,int image,String desc){selectedId=id;selectedName=name;selectedPhoto=image;frame("Detalle del producto");ImageView img=new ImageView(this);img.setImageResource(image);img.setScaleType(ImageView.ScaleType.CENTER_CROP);body.addView(img,new LinearLayout.LayoutParams(-1,310));body.addView(text(name,25,true));body.addView(text(desc,17,false));body.addView(text("Disponibilidad: por confirmar",15,false));body.addView(button("Nueva solicitud",this::newOrder));}
    private void newOrder(){frame("Nueva solicitud");ImageView img=new ImageView(this);img.setImageResource(selectedPhoto);body.addView(img,new LinearLayout.LayoutParams(-1,175));body.addView(text(selectedName,23,true));EditText qty=field("Cantidad",false);qty.setInputType(2);qty.setText("1");body.addView(button("Enviar solicitud",()->{
        if(token.isEmpty()){auth();Toast.makeText(this,"Inicia sesión primero",Toast.LENGTH_SHORT).show();return;}
        try{int n=Integer.parseInt(qty.getText().toString());if(n<1||n>99)throw new Exception("Cantidad entre 1 y 99");
            JSONObject payload=new JSONObject().put("lineas",new JSONArray().put(new JSONObject().put("productoId",selectedId).put("cantidad",n)));
            request("/orders","POST",payload,data->{Toast.makeText(this,"Pedido "+data.getString("codigo")+" creado",Toast.LENGTH_LONG).show();orders();});
        }catch(Exception e){fail(e);}
    }));}
    private void orders(){frame("Mis pedidos");if(token.isEmpty()){body.addView(text("Ingresa para ver tu historial.",16,false));body.addView(button("Ingresar",this::auth));return;}
        request("/orders/mine","GET",null,data->{JSONArray list=data.getJSONArray("items");if(list.length()==0)body.addView(text("Aún no tienes pedidos.",17,false));
            for(int i=0;i<list.length();i++){JSONObject o=list.getJSONObject(i);body.addView(text(o.getString("codigo")+" · "+o.getString("estado"),21,true));body.addView(text("Total ilustrativo: S/ "+o.optString("total"),16,false));
                JSONArray lines=o.getJSONArray("lineas");for(int j=0;j<lines.length();j++){JSONObject line=lines.getJSONObject(j);body.addView(text(line.getString("nombre")+" × "+line.getInt("cantidad"),15,false));}
            }
        });
    }
    @Override protected void onDestroy(){pool.shutdownNow();super.onDestroy();}
}
