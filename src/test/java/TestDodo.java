import io.github.minecraftchampions.dodoopenjava.api.v2.IslandApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.MemberApi;
import org.junit.Test;

import java.io.IOException;

public class TestDodo {
    @Test
    public void test() throws IOException {
        System.out.println(IslandApi.getIslandInfo("47657182","NDc2NTcxODI.77-977-9woY.RqdwnzsoyWFJOey5zcfeM9pvsErjh5-jdnqeLoVbEsM","152331"));
        System.out.println(MemberApi.getMemberList("47657182","NDc2NTcxODI.77-977-9woY.RqdwnzsoyWFJOey5zcfeM9pvsErjh5-jdnqeLoVbEsM","152331",0,100));
        System.out.println(MemberApi.getMemberInfo("47657182","NDc2NTcxODI.77-977-9woY.RqdwnzsoyWFJOey5zcfeM9pvsErjh5-jdnqeLoVbEsM","152331","2183323"));
    }
}
