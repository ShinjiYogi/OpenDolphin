package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.infomodel.UserModel;

/**
 * RemoteSystemService
 *
 * @author Minagawa, Kazushi
 */
public interface RemoteSystemService {
    
    /**
     * ����OID���擾����B
     *
     * @return OID
     */
    public String helloDolphin();
    
    /**
     * �{�݂ƊǗ��ҏ���o�^����B
     * @param user �{�݊Ǘ���
     */
    public void addFacilityAdmin(UserModel user);
    
}