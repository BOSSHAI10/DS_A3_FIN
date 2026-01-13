package com.example.chat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RuleBasedService {

    private final Map<String, String> knowledgeBase;
    private final AIService aiService;

    @Autowired
    public RuleBasedService(AIService aiService) {
        this.aiService = aiService;
        this.knowledgeBase = new HashMap<>();
        initializeRules();
    }

    private void initializeRules() {
        knowledgeBase.put("cum pot vedea istoricul consumului?", "Poți vizualiza graficele detaliate de consum în secțiunea 'Statistici' din meniul principal.");
        knowledgeBase.put("cum adaug un dispozitiv nou?", "Adăugarea dispozitivelor este permisă doar administratorilor. Te rugăm să contactezi suportul tehnic.");
        knowledgeBase.put("primesc notificări dacă consum prea mult?", "Da, sistemul trimite automat o alertă când un dispozitiv depășește limita maximă de consum configurată.");
        knowledgeBase.put("ce fac dacă un senzor nu merge?", "Verifică dacă dispozitivul este conectat la priză și la internet. Dacă problema persistă, contactează adminul.");
        knowledgeBase.put("cum pot economisi energie?", "Îți recomandăm să folosești becuri LED, să scoți din priză încărcătoarele nefolosite și să monitorizezi orele de vârf.");
        knowledgeBase.put("pot schimba parola contului?", "Da, poți modifica parola accesând pagina 'Profilul Meu' și selectând 'Schimbă Parola'.");
        knowledgeBase.put("care este prețul per kwh?", "Prețul este stabilit în contractul tău de furnizare. Sistemul nostru monitorizează doar cantitatea consumată.");
        knowledgeBase.put("cine este administratorul sistemului?", "Administratorul este persoana responsabilă cu gestionarea utilizatorilor și a dispozitivelor din clădirea ta.");
        knowledgeBase.put("ce este un smart meter?", "Este un contor inteligent care măsoară consumul în timp real și îl transmite către serverul nostru pentru analiză.");
        knowledgeBase.put("care este programul de suport?", "Echipa noastră tehnică este disponibilă de Luni până Vineri, între orele 09:00 și 17:00.");
    }

    public String getRuleBasedResponse(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Nu am înțeles mesajul.";
        }
        String normalizedMessage = message.trim().toLowerCase();

        if (knowledgeBase.containsKey(normalizedMessage)) {
            return knowledgeBase.get(normalizedMessage);
        }
        if (normalizedMessage.contains("admin") || normalizedMessage.contains("contact")) {
            return "ADMIN_REQUEST: Cererea ta a fost trimisă către un administrator.";
        }

        try {
            return aiService.generateResponse(message);
        } catch (Exception e) {
            System.err.println("AI Service Error: " + e.getMessage());
            return "Îmi pare rău, nu am un răspuns predefinit și serviciul AI este indisponibil momentan.";
        }
    }
}