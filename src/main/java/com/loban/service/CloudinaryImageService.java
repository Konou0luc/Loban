package com.loban.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.loban.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class CloudinaryImageService {

    private static final String PROFILE_FOLDER = "loban/profiles";
    /** ~2 Mo côté entrée avant upload */
    private static final int MAX_BYTES = 2_500_000;

    private final Cloudinary cloudinary;

    public CloudinaryImageService(org.springframework.beans.factory.ObjectProvider<Cloudinary> cloudinaryProvider) {
        this.cloudinary = cloudinaryProvider.getIfAvailable();
    }

    public boolean isConfigured() {
        return cloudinary != null;
    }

    /**
     * Envoie les octets image vers Cloudinary et renvoie l’URL HTTPS sécurisée.
     */
    public String uploadProfilePhoto(byte[] imageBytes, long userId) {
        if (cloudinary == null) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Stockage Cloudinary non configuré. Ajoutez CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, "
                            + "CLOUDINARY_API_SECRET et CLOUDINARY_ENABLED=true dans backend/.env puis redémarrez.");
        }
        if (imageBytes.length > MAX_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image trop volumineuse (maximum environ 2,5 Mo).");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result =
                    cloudinary.uploader().upload(
                            imageBytes,
                            ObjectUtils.asMap(
                                    "folder", PROFILE_FOLDER,
                                    "public_id", "profile-user-" + userId,
                                    "overwrite", true,
                                    "resource_type", "image"));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Réponse Cloudinary sans URL sécurisée.");
            }
            return url.toString();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Échec de l’envoi de l’image vers Cloudinary.", e);
        }
    }

    /** Décode une data URL {@code data:image/...;base64,...}. */
    public static byte[] decodeDataUrlBase64(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        if (comma < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Format d’image invalide.");
        }
        String header = dataUrl.substring(0, comma);
        if (!header.contains("base64")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Seules les images encodées en base64 sont acceptées.");
        }
        String b64 = dataUrl.substring(comma + 1).replaceAll("\\s", "");
        try {
            return Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Décodage base64 de l’image impossible.");
        }
    }
}
