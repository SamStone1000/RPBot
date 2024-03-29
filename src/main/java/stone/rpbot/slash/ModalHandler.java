package stone.rpbot.slash;

import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public interface ModalHandler {
    public void onModal(Map<String, ModalMapping> event);
}
