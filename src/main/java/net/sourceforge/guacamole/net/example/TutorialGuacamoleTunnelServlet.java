package org.glyptodon.guacamole.net.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.GuacamoleSocket;
import org.glyptodon.guacamole.net.GuacamoleTunnel;
import org.glyptodon.guacamole.net.InetGuacamoleSocket;
import org.glyptodon.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.glyptodon.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.glyptodon.guacamole.servlet.GuacamoleSession;
import org.glyptodon.guacamole.net.SimpleGuacamoleTunnel;

public class TutorialGuacamoleTunnelServlet
    extends GuacamoleHTTPTunnelServlet {

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
        throws GuacamoleException {

        StringBuffer url = request.getRequestURL();
        Pattern pattern = Pattern.compile("type([a-zA-Z]+)/host([0-9a-zA-Z]+)/port([0-9]+)/username([a-zA-Z]+)/password([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(url);
        
        String type = "";
		String host = "";
		String port = "";
		String username = "";
		String password = "";
		if (matcher.find()) {
			type = matcher.group(1);
			host = matcher.group(2);
			port = matcher.group(3);
			username = matcher.group(4);
			password = matcher.group(5);
		}
		
        // Create our configuration
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol(type);
        config.setParameter("hostname", host);
        config.setParameter("port", port);
        config.setParameter("password", password);
        if ( type != "vnc")
            config.setParameter("username",username);
        else 
            {
            }
        // Connect to guacd - everything is hard-coded here.
        String guacd_ip = System.getenv("GUACD_PORT_4822_TCP_ADDR");
        int guacd_port = Integer.parseInt(System.getenv("GUACD_PORT_4822_TCP_PORT"));
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(guacd_ip,guacd_port),
                config
        );

        // Establish the tunnel using the connected socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);

        // Attach tunnel to session
        HttpSession httpSession = request.getSession(true);
        GuacamoleSession session = new GuacamoleSession(httpSession);
        session.attachTunnel(tunnel);

        // Return pre-attached tunnel
        return tunnel;

    }

}
