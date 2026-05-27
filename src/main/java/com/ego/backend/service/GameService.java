package com.ego.backend.service;

import com.ego.backend.entity.Item;
import com.ego.backend.entity.Room;
import com.ego.backend.entity.User;
import com.ego.backend.repository.ItemRepository;
import com.ego.backend.repository.RoomRepository;
import com.ego.backend.repository.UserRepository;
import com.ego.backend.entity.UserItem;
import com.ego.backend.entity.UserRoomState;
import com.ego.backend.repository.UserItemRepository;
import com.ego.backend.repository.UserRoomStateRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GameService {

    private final RoomRepository roomRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserItemRepository userItemRepository;
    private final UserRoomStateRepository userRoomStateRepository;

    public GameService(
            RoomRepository roomRepository,
            ItemRepository itemRepository,
            UserRepository userRepository,
            UserItemRepository userItemRepository,
            UserRoomStateRepository userRoomStateRepository
    ) {
        this.roomRepository = roomRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.userItemRepository = userItemRepository;
        this.userRoomStateRepository = userRoomStateRepository;
    }

    private UserRoomState getOrCreateRoomState(
            Long userId,
            Long itemId
    ) {

        return userRoomStateRepository
                .findByUserIdAndItemId(userId, itemId)
                .orElseGet(() -> {

                    UserRoomState state = new UserRoomState();

                    state.setUserId(userId);
                    state.setItemId(itemId);
                    state.setInteractionState(0);

                    return userRoomStateRepository.save(state);
                });
    }

    public Map<String, Object> processCommand(String input, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (user.getCurrentRoomId() == null) {
            user.setCurrentRoomId(1L);
            userRepository.save(user);
        }

        String command = input.toLowerCase().trim();

        Room currentRoom = roomRepository.findById(user.getCurrentRoomId())
                .orElseThrow(() -> new RuntimeException("Stanza non trovata"));

        String message = "Comando non riconosciuto.";

        if (user.getCurrentRoomId() == null) {
            user.setCurrentRoomId(1L);
            userRepository.save(user);
        }

        if (command.startsWith("vai a ")) {
            String direction = command.substring(6).trim();
            Long nextRoomId = currentRoom.getExits().get(direction);

            if (nextRoomId != null) {
                Room nextRoom = roomRepository.findById(nextRoomId).orElse(null);
                boolean isUnlockedForUser = userRoomStateRepository
                        .findByUserIdAndRoomId(user.getId(), nextRoomId)
                        .map(s -> Boolean.TRUE.equals(s.getUnlocked()))
                        .orElse(false);

                if (nextRoom != null && (nextRoom.isExplorable() || isUnlockedForUser)) {
                    user.setCurrentRoomId(nextRoomId);
                    userRepository.save(user);

                    currentRoom = nextRoom;
                    message = "Ti sposti verso " + direction + ".\n" +
                            currentRoom.getDescription();
                } else {
                    message = "La strada verso " + direction + " è bloccata.";
                }
            } else {
                message = "Non puoi andare in quella direzione.";
            }
        } else if (command.equals("guarda")) {
            message = currentRoom.getDescription();
        } else if (command.startsWith("prendi ")) {
            message = takeItem(command.substring(7).trim(), username);
        } else if (command.startsWith("leggi ")) {
            message = readItem(command.substring(6).trim(), username);
        } else if (command.equals("inventario") || command.equals("i")) {
            message = showInventory(username);
        } else if (command.contains("raddrizza sedia")) {
            message = interactWithChair("raddrizza", username);
        } else if (command.contains("sposta") || command.contains("posiziona")) {
            boolean hasChair = !itemRepository
                    .findAllByNameIgnoreCaseAndRoomId(
                            "sedia",
                            user.getCurrentRoomId()
                    )
                    .isEmpty();

            if (!hasChair) {
                message = "Non c'è nulla da spostare qui.";
            } else if (command.contains("sotto il baule") || command.contains("sotto la cassa") || command.contains("sotto cassa") || command.contains("sotto baule")) {
                message = interactWithChair("sposta", username);
            } else {
                message = "Dove vorresti spostare la sedia?";
            }
        } else if (command.contains("sali su sedia") || command.contains("usa sedia")) {
            message = interactWithChair("sali", username);
        } else if (command.contains("cassa") || command.contains("baule")) {
            message = interactWithChest(username);
        } else if (command.contains("apri scatola")) {
            message = openBox(username);
        } else if (command.contains("pescare") || command.contains("pesca")) {
            message = fish(username);
        } else if (command.contains("usa chiave")) {
            if (user.getCurrentRoomId() == 4L) {
                UserRoomState temploState = userRoomStateRepository
                        .findByUserIdAndRoomId(user.getId(), 8L)
                        .orElseGet(() -> {
                            UserRoomState s = new UserRoomState();
                            s.setUserId(user.getId());
                            s.setRoomId(8L);
                            s.setUnlocked(false);
                            return s;
                        });
                if (Boolean.TRUE.equals(temploState.getUnlocked())) {
                    message = "Il portale del Tempio Onirico è già spalancato.";
                } else {
                    temploState.setUnlocked(true);
                    userRoomStateRepository.save(temploState);
                    message = "Usi la chiave. Senti un boato: il Tempio Onirico a nord è ora accessibile!";
                }
            } else {
                message = "Non vedi alcuna serratura qui dove usare la chiave.";
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("currentRoom", getCurrentRoom(username));

        return response;
    }

    public Room getCurrentRoom(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        return roomRepository.findById(user.getCurrentRoomId())
                .orElseThrow(() -> new RuntimeException("Impossibile recuperare la stanza attuale."));
    }

    public String takeItem(String itemName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<Item> itemOpt = itemRepository.findAllByNameIgnoreCaseAndRoomId(
                itemName,
                user.getCurrentRoomId()
        );

        if (itemOpt.isEmpty()) {
            return "Non c'è nessun oggetto chiamato " + itemName + " in questa stanza.";
        }

        Item item = itemOpt.getFirst();

        if (!item.isPickable()) {
            return "Non puoi prendere " + item.getName() + "." +
                    (item.getName().contains("Foglio")
                            ? " Puoi solo leggerlo."
                            : " Sembra fissato al suolo o troppo pesante.");
        }

        if (item.getName().equalsIgnoreCase("Foglia di Edera")) {
            user.setCurrentRoomId(1L);
            userRepository.save(user);

            return "Appena tocchi la " + item.getName() +
                    ", un vapore tossico ti avvolge... Tutto si fa buio...\n" +
                    "Ti svegli...\n" +
                    getCurrentRoom(username).getDescription();
        }

        boolean alreadyCollected =
                userItemRepository.existsByUserIdAndItemId(
                        user.getId(),
                        item.getId()
                );

        if (alreadyCollected) {
            return "Hai già raccolto questo oggetto.";
        }

        UserItem userItem = new UserItem();

        userItem.setUserId(user.getId());
        userItem.setItemId(item.getId());
        userItem.setCollected(true);

        userItemRepository.save(userItem);

        return "Hai raccolto " + item.getName() + ". " + item.getDescription();
    }

    public String showInventory(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<UserItem> inventory =
                userItemRepository.findByUserId(user.getId())
                        .stream()
                        .filter(UserItem::getCollected)
                        .toList();

        if (inventory.isEmpty()) {
            return "Il tuo inventario è vuoto.";
        }

        StringBuilder sb = new StringBuilder("Porti con te:\n");

        for (UserItem userItem : inventory) {

            itemRepository
                    .findById(userItem.getItemId()).ifPresent(item -> sb.append("- ")
                            .append(item.getName())
                            .append(": ")
                            .append(item.getDescription())
                            .append("\n"));

        }

        return sb.toString();
    }

    public String readItem(String itemName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<Item> itemRoom = itemRepository.findAllByNameIgnoreCaseAndRoomId(
                itemName,
                user.getCurrentRoomId()
        );

        List<UserItem> inventory =
                userItemRepository.findByUserId(user.getId())
                        .stream()
                        .filter(UserItem::getCollected)
                        .toList();

        Item itemInv = inventory.stream()
                .map(userItem -> itemRepository
                        .findById(userItem.getItemId())
                        .orElse(null))
                .filter(Objects::nonNull)
                .filter(i -> i.getName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);

        Item item = !itemRoom.isEmpty() ? itemRoom.getFirst() : itemInv;

        if (item == null) {
            return "Non c'è nessun oggetto chiamato " + itemName + " da leggere.";
        }

        if ("READABLE".equals(item.getEffect())) {
            if (item.getName().equalsIgnoreCase("Diario del Goblin")) {
                return item.getDescription();
            }

            return "Sul " + item.getName() + " c'è scritto: " + item.getDescription();
        }

        return "Non puoi leggere " + item.getName() +
                ". Non sembra contenere informazioni leggibili.";
    }

    public String interactWithChair(String action, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<Item> sedie = itemRepository.findAllByNameIgnoreCaseAndRoomId(
                "Sedia",
                user.getCurrentRoomId()
        );



        Item sedia = sedie.isEmpty() ? null : sedie.getFirst();

        if (sedia == null) {
            return "Non c'è nessuna sedia in questa stanza.";
        }

        UserRoomState chairState =
                getOrCreateRoomState(
                        user.getId(),
                        sedia.getId()
                );

        if (action.equalsIgnoreCase("raddrizza")) {
            if (chairState.getInteractionState() >= 1) {
                return "La sedia è già in posizione verticale.";
            }

            chairState.setInteractionState(1);

            userRoomStateRepository.save(chairState);
            return "Hai raddrizzato la sedia.";
        }

        if (action.equalsIgnoreCase("sposta")) {
            if (chairState.getInteractionState() == 0) {
                return "La sedia è rovesciata, dovresti raddrizzarla prima.";
            }

            if (chairState.getInteractionState() == 2) {
                return "La sedia è già in posizione.";
            }

            chairState.setInteractionState(2);

            userRoomStateRepository.save(chairState);

            return "Trascini la sedia con un rumore stridente finché non si trova esattamente sotto il baule appeso.";
        }

        if (action.equalsIgnoreCase("sali")) {
            if (chairState.getInteractionState() < 1) {
                return "La sedia è rovesciata, non puoi salirci.";
            }

            if (chairState.getInteractionState() == 1) {
                return "Sali sulla sedia, ma da qui arrivi solo al soffitto vuoto. Forse dovresti spostarla dove serve.";
            }

            return "Sali sulla sedia posizionata sotto il baule. Ora sei faccia a faccia con il baule.";
        }

        return "Non puoi fare questa azione con la sedia.";
    }

    public String interactWithChest(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<Item> sedie = itemRepository.findAllByNameIgnoreCaseAndRoomId(
                "Sedia",
                user.getCurrentRoomId()
        );

        Item sedia = sedie.isEmpty() ? null : sedie.getFirst();

        if (sedia != null) {

            UserRoomState chairState =
                    getOrCreateRoomState(
                            user.getId(),
                            sedia.getId()
                    );

            if (chairState.getInteractionState() == 2) {

                return "Con la sedia posizionata sotto il baule riesci a raggiungerlo e a frugare al suo interno. Trovi una chiave arrugginita!";

            }
        }

        return "Il baule pende troppo in alto. Non riesci a raggiungerlo a mani nude.";
    }

    public String fish(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (user.getCurrentRoomId() != 7L) {
            return "Non c'è nessun posto adatto per pescare qui.";
        }

        List<UserItem> inventory =
                userItemRepository.findByUserId(user.getId());

        boolean hasFishingRod = inventory.stream()
                .map(userItem -> itemRepository
                        .findById(userItem.getItemId())
                        .orElse(null))
                .filter(Objects::nonNull)
                .anyMatch(item -> item.getName().equalsIgnoreCase("Canna da Pesca"));

        if (!hasFishingRod) {
            return "Non hai una canna da pesca per pescare.";
        }

        List<Item> boxes = itemRepository.findAllByNameIgnoreCase("Scatola");

        Item box = boxes.stream()
                .filter(item -> item.getRoomId() != null && item.getRoomId() == 7L)
                .filter(item -> !userItemRepository
                        .existsByUserIdAndItemId(user.getId(), item.getId()))
                .findFirst()
                .orElse(null);

        if (box != null) {
            UserItem userItem = new UserItem();
            userItem.setUserId(user.getId());
            userItem.setItemId(box.getId());
            userItem.setCollected(true);

            userItemRepository.save(userItem);

            return "Lanci l'amo e attendi... Senti un peso insolito. " +
                    "Tirando con forza, recuperi una SCATOLA DI LEGNO incrostata di alghe invece di un pesce!";
        }

        return "Peschi per un po', ma i pesci sembrano troppo furbi oggi. Non abbocca nulla.";
    }

    public String openBox(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        List<UserItem> inventory =
                userItemRepository.findByUserId(user.getId());

        Item box = inventory.stream()
                .map(userItem -> itemRepository
                        .findById(userItem.getItemId())
                        .orElse(null))
                .filter(Objects::nonNull)
                .filter(i -> i.getName().equalsIgnoreCase("Scatola"))
                .findFirst()
                .orElse(null);

        if (box == null) {
            return "Non hai nessuna scatola da aprire.";
        }

        UserRoomState boxState =
                getOrCreateRoomState(
                        user.getId(),
                        box.getId()
                );

        if (boxState.getInteractionState() == 1) {
            return "La scatola è già aperta, dentro c'è solo fango e alghe.";
        }

        boxState.setInteractionState(1);
        userRoomStateRepository.save(boxState);

        Item key = itemRepository.findById(999L)
                .orElseThrow();

        boolean alreadyOwned =
                userItemRepository.existsByUserIdAndItemId(
                        user.getId(),
                        key.getId()
                );

        if (!alreadyOwned) {

            UserItem userItem = new UserItem();
            userItem.setUserId(user.getId());
            userItem.setItemId(key.getId());
            userItem.setCollected(true);

            userItemRepository.save(userItem);
        }

        return "Forzi il coperchio della scatola... Senti uno scatto e il coperchio si alza leggermente. " +
                "All'interno, tra residui di fango e alghe, trovi una CHIAVE PESANTE.";
    }
}