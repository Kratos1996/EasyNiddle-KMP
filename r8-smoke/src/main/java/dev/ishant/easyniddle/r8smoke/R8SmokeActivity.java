package dev.ishant.easyniddle.r8smoke;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import dev.ishant.easyniddle.NiddleRoute;

/**
 * Tiny Android consumer used to ensure the library remains consumable by an
 * optimized release build. The route marker is deliberately referenced from
 * app bytecode so R8 must process the library type.
 */
public final class R8SmokeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NiddleRoute route = new SmokeRoute();
        boolean routeTypeAvailable = NiddleRoute.class.isAssignableFrom(route.getClass());

        TextView status = new TextView(this);
        status.setText(routeTypeAvailable ? "EasyNiddle R8 smoke passed" : "EasyNiddle R8 smoke failed");
        setContentView(status);
    }

    private static final class SmokeRoute implements NiddleRoute {
    }
}
