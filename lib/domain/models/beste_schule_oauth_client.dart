import 'package:oauth2_client/oauth2_client.dart';

class BesteSchuleOauthClient extends OAuth2Client {
  static String authorizeUrlString = "https://beste.schule/oauth/authorize";
  static String tokenUrlString = "https://beste.schule/oauth/token";

  BesteSchuleOauthClient({
    required super.redirectUri,
    required super.customUriScheme,
  }) : super(
         authorizeUrl: 'https://beste.schule/oauth/authorize',
         tokenUrl: 'https://beste.schule/oauth/token',
       );
}
