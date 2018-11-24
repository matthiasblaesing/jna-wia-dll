package libs;

import com.sun.jna.Callback;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public interface Win32Twain extends Library {

    public static final Win32Twain INSTANCE = Native.loadLibrary(
            "TWAINDSM",
            Win32Twain.class);

    public static class TW_STATUS extends TWStructure {

        public short ConditionCode;
        public short Reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"ConditionCode", "Reserved"});
        }
    }

    public static class TW_VERSION extends TWStructure {

        public short MajorNum;
        public short MinorNum;
        public short Language;
        public short Country;
        public byte Info[] = new byte[34];

        public void setInfo(String m) {
            setSTR32(Info, m);
        }

        public String getInfo() {
            return Native.toString(Info);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"MajorNum", "MinorNum", "Language", "Country", "Info"});
        }
    }

    /*typedef struct {
   TW_UINT32  Id;              // Unique number.  In Windows, application hWnd      
   TW_VERSION Version;         // Identifies the piece of code              
   TW_UINT16  ProtocolMajor;   // Application and DS must set to TWON_PROTOCOLMAJOR 
   TW_UINT16  ProtocolMinor;   // Application and DS must set to TWON_PROTOCOLMINOR 
   TW_UINT32  SupportedGroups; // Bit field OR combination of DG_ constants 
   TW_STR32   Manufacturer;    // Manufacturer name, e.g. "Hewlett-Packard" 
   TW_STR32   ProductFamily;   // Product family name, e.g. "ScanJet"       
   TW_STR32   ProductName;     // Product name, e.g. "ScanJet Plus"         
} TW_IDENTITY, FAR * pTW_IDENTITY;*/
    public static class TW_IDENTITY extends TWStructure {

        public int Id;
        public TW_VERSION Version = new TW_VERSION();
        public short ProtocolMajor;
        public short ProtocolMinor;
        public int SupportedGroups;
        public byte Manufacturer[] = new byte[34];
        public byte ProductFamily[] = new byte[34];
        public byte ProductName[] = new byte[34];

        public void setManufacturer(String m) {
            setSTR32(Manufacturer, m);
        }

        public String getManufacturer() {
            return Native.toString(Manufacturer);
        }

        public void setProductFamily(String m) {
            setSTR32(ProductFamily, m);
        }

        public String getProductFamily() {
            return Native.toString(ProductFamily);
        }

        public void setProductName(String m) {
            setSTR32(ProductName, m);
        }

        public String getProductName() {
            return Native.toString(ProductName);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Id", "Version", "ProtocolMajor", "ProtocolMinor", "SupportedGroups", "Manufacturer", "ProductFamily", "ProductName"});
        }
    }

    /*typedef struct {
    TW_INT16     Whole;        // maintains the sign 
    TW_UINT16    Frac;
} TW_FIX32,  FAR *pTW_FIX32;*/
    public static class TW_FIX32 extends TWStructure {

        public short Whole;
        public short Frac;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Whole", "Frac"});
        }
    }

    /*typedef struct {
   TW_FIX32   XResolution;      // Resolution in the horizontal             
   TW_FIX32   YResolution;      // Resolution in the vertical               
   TW_INT32   ImageWidth;       // Columns in the image, -1 if unknown by DS
   TW_INT32   ImageLength;      // Rows in the image, -1 if unknown by DS   
   TW_INT16   SamplesPerPixel;  // Number of samples per pixel, 3 for RGB   
   TW_INT16   BitsPerSample[8]; // Number of bits for each sample           
   TW_INT16   BitsPerPixel;     // Number of bits for each padded pixel     
   TW_BOOL    Planar;           // True if Planar, False if chunky          
   TW_INT16   PixelType;        // How to interp data; photo interp (TWPT_) 
   TW_UINT16  Compression;      // How the data is compressed (TWCP_xxxx)   
} TW_IMAGEINFO, FAR * pTW_IMAGEINFO;*/
    public static class TW_IMAGEINFO extends TWStructure {

        public TW_FIX32 XResolution;
        public TW_FIX32 YResolution;
        public int ImageWidth;
        public int ImageLength;
        public short SamplesPerPixel;
        public short BitsPerSample[] = new short[8];
        public short BitsPerPixel;
        public boolean Planar;
        public short PixelType;
        public short Compression;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"XResolution", "YResolution", "ImageWidth", "ImageLength", "SamplesPerPixel", "BitsPerSample", "BitsPerPixel", "Planar", "PixelType", "Compression"});
        }
    }

    /*typedef struct tagRGBQUAD {
  BYTE rgbBlue;
  BYTE rgbGreen;
  BYTE rgbRed;
  BYTE rgbReserved;
}RGBQUAD;*/
    public static class RGBQUAD extends TWStructure {

        public byte rgbBlue;
        public byte rgbGreen;
        public byte rgbRed;
        public byte rgbReserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"rgbBlue", "rgbGreen", "rgbRed", "rgbReserved"});
        }
    }

    /*typedef struct {
   TW_BOOL    ShowUI;  // TRUE if DS should bring up its UI           
   TW_BOOL    ModalUI; // For Mac only - true if the DS's UI is modal 
   TW_HANDLE  hParent; // For windows only - Application window handle        
} TW_USERINTERFACE, FAR * pTW_USERINTERFACE;*/
    public static class TW_USERINTERFACE extends TWStructure {

        public short ShowUI;
        public short ModalUI;
        public HWND hParent;

        public boolean isShowUI() {
            return ShowUI != 0;
        }

        public boolean isModalUI() {
            return ModalUI != 0;
        }

        public void setShowUI(boolean show) {
            ShowUI = (short) (show ? 0xFFFF : 0);
        }

        public void setModalUI(boolean modal) {
            ModalUI = (short) (modal ? 0xFFFF : 0);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"ShowUI", "ModalUI", "hParent"});
        }
    }

    public static class TW_PENDINGXFERS extends TWStructure {

        public short Count;
        public int EOJ;
        public int Reserved;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Count", "EOJ", "Reserved"});
        }
    }

    public static class TW_EVENT extends TWStructure {

        public Pointer pEvent;
        public short TWMessage;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"pEvent", "TWMessage"});
        }
    }

    public static class BITMAPINFOHEADER extends TWStructure {

        public BITMAPINFOHEADER() {
        }

        public BITMAPINFOHEADER(Pointer p) {
            super(p);
        }

        public int biSize;
        public int biWidth;
        public int biHeight;
        public short biPlanes;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"biSize", "biWidth", "biHeight", "biPlanes", "biBitCount", "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter", "biClrUsed", "biClrImportant"});
        }
    }

    public static interface DSMENTRYPROC extends Callback {
        public short callback(TW_IDENTITY pOrigin, TW_IDENTITY pDest, int DG, short DAT, short MSG, Pointer pData);
    }

    public static interface DSM_MEMALLOCATE extends Callback {
        public HANDLE callback(int size);
    }

    public static interface DSM_MEMFREE extends Callback {
        public void callback(HANDLE handle);
    }

    public static interface DSM_MEMLOCK extends Callback {
        public Pointer callback(HANDLE handle);
    }

    public static interface DSM_MEMUNLOCK extends Callback {
        public void callback(HANDLE handle);
    }

    public static class TW_ENTRYPOINT extends TWStructure {
        public int Size;
        public DSMENTRYPROC DSM_Entry;
        public DSM_MEMALLOCATE DSM_MemAllocate;
        public DSM_MEMFREE DSM_MemFree;
        public DSM_MEMLOCK DSM_MemLock;
        public DSM_MEMUNLOCK DSM_MemUnlock;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(new String[]{"Size", "DSM_Entry", "DSM_MemAllocate", "DSM_MemFree", "DSM_MemLock", "DSM_MemUnlock"});
        }

        @Override
        public void write() {
            this.Size = size();
            super.write();
        }
    }

    public short DSM_Entry(TW_IDENTITY origin, TW_IDENTITY destination, int dg, short dat, short msg, Pointer p);

    public short DSM_Entry(TW_IDENTITY origin, TW_IDENTITY destination, int dg, short dat, short msg, Structure p);

    public short DSM_Entry(TW_IDENTITY origin, TW_IDENTITY destination, int dg, short dat, short msg, PointerType p);

    static abstract class TWStructure extends Structure {

        public TWStructure() {
        }

        public TWStructure(Pointer p) {
            super(p);
        }

        @Override
        protected int getNativeAlignment(Class<?> type, Object value, boolean isFirstElement) {
            int superValue = super.getNativeAlignment(type, value, isFirstElement);
            return Math.min(2, superValue);
        }

        public static void setSTR32(byte[] target, String input) {
            assert target != null;
            assert target.length == 34;
            Arrays.fill(target, (byte) 0);
            byte[] data = Native.toByteArray(input);
            System.arraycopy(data, 0, target, 0, Math.min(32, data.length));
        }
    }

    short TWLG_USERLOCALE = -1;
    short TWLG_DAN = 0;
    short TWLG_DUT = 1;
    short TWLG_ENG = 2;
    short TWLG_FCF = 3;
    short TWLG_FIN = 4;
    short TWLG_FRN = 5;
    short TWLG_GER = 6;
    short TWLG_ICE = 7;
    short TWLG_ITN = 8;
    short TWLG_NOR = 9;
    short TWLG_POR = 10;
    short TWLG_SPA = 11;
    short TWLG_SWE = 12;
    short TWLG_USA = 13;
    short TWLG_AFRIKAANS = 14;
    short TWLG_ALBANIA = 15;
    short TWLG_ARABIC = 16;
    short TWLG_ARABIC_ALGERIA = 17;
    short TWLG_ARABIC_BAHRAIN = 18;
    short TWLG_ARABIC_EGYPT = 19;
    short TWLG_ARABIC_IRAQ = 20;
    short TWLG_ARABIC_JORDAN = 21;
    short TWLG_ARABIC_KUWAIT = 22;
    short TWLG_ARABIC_LEBANON = 23;
    short TWLG_ARABIC_LIBYA = 24;
    short TWLG_ARABIC_MOROCCO = 25;
    short TWLG_ARABIC_OMAN = 26;
    short TWLG_ARABIC_QATAR = 27;
    short TWLG_ARABIC_SAUDIARABIA = 28;
    short TWLG_ARABIC_SYRIA = 29;
    short TWLG_ARABIC_TUNISIA = 30;
    short TWLG_ARABIC_UAE = 31;
    short TWLG_ARABIC_YEMEN = 32;
    short TWLG_BASQUE = 33;
    short TWLG_BYELORUSSIAN = 34;
    short TWLG_BULGARIAN = 35;
    short TWLG_CATALAN = 36;
    short TWLG_CHINESE = 37;
    short TWLG_CHINESE_HONGKONG = 38;
    short TWLG_CHINESE_PRC = 39;
    short TWLG_CHINESE_SINGAPORE = 40;
    short TWLG_CHINESE_SIMPLIFIED = 41;
    short TWLG_CHINESE_TAIWAN = 42;
    short TWLG_CHINESE_TRADITIONAL = 43;
    short TWLG_CROATIA = 44;
    short TWLG_CZECH = 45;
    short TWLG_DANISH = TWLG_DAN;
    short TWLG_DUTCH = TWLG_DUT;
    short TWLG_DUTCH_BELGIAN = 46;
    short TWLG_ENGLISH = TWLG_ENG;
    short TWLG_ENGLISH_AUSTRALIAN = 47;
    short TWLG_ENGLISH_CANADIAN = 48;
    short TWLG_ENGLISH_IRELAND = 49;
    short TWLG_ENGLISH_NEWZEALAND = 50;
    short TWLG_ENGLISH_SOUTHAFRICA = 51;
    short TWLG_ENGLISH_UK = 52;
    short TWLG_ENGLISH_USA = TWLG_USA;
    short TWLG_ESTONIAN = 53;
    short TWLG_FAEROESE = 54;
    short TWLG_FARSI = 55;
    short TWLG_FINNISH = TWLG_FIN;
    short TWLG_FRENCH = TWLG_FRN;
    short TWLG_FRENCH_BELGIAN = 56;
    short TWLG_FRENCH_CANADIAN = TWLG_FCF;
    short TWLG_FRENCH_LUXEMBOURG = 57;
    short TWLG_FRENCH_SWISS = 58;
    short TWLG_GERMAN = TWLG_GER;
    short TWLG_GERMAN_AUSTRIAN = 59;
    short TWLG_GERMAN_LUXEMBOURG = 60;
    short TWLG_GERMAN_LIECHTENSTEIN = 61;
    short TWLG_GERMAN_SWISS = 62;
    short TWLG_GREEK = 63;
    short TWLG_HEBREW = 64;
    short TWLG_HUNGARIAN = 65;
    short TWLG_ICELANDIC = TWLG_ICE;
    short TWLG_INDONESIAN = 66;
    short TWLG_ITALIAN = TWLG_ITN;
    short TWLG_ITALIAN_SWISS = 67;
    short TWLG_JAPANESE = 68;
    short TWLG_KOREAN = 69;
    short TWLG_KOREAN_JOHAB = 70;
    short TWLG_LATVIAN = 71;
    short TWLG_LITHUANIAN = 72;
    short TWLG_NORWEGIAN = TWLG_NOR;
    short TWLG_NORWEGIAN_BOKMAL = 73;
    short TWLG_NORWEGIAN_NYNORSK = 74;
    short TWLG_POLISH = 75;
    short TWLG_PORTUGUESE = TWLG_POR;
    short TWLG_PORTUGUESE_BRAZIL = 76;
    short TWLG_ROMANIAN = 77;
    short TWLG_RUSSIAN = 78;
    short TWLG_SERBIAN_LATIN = 79;
    short TWLG_SLOVAK = 80;
    short TWLG_SLOVENIAN = 81;
    short TWLG_SPANISH = TWLG_SPA;
    short TWLG_SPANISH_MEXICAN = 82;
    short TWLG_SPANISH_MODERN = 83;
    short TWLG_SWEDISH = TWLG_SWE;
    short TWLG_THAI = 84;
    short TWLG_TURKISH = 85;
    short TWLG_UKRANIAN = 86;
    short TWLG_ASSAMESE = 87;
    short TWLG_BENGALI = 88;
    short TWLG_BIHARI = 89;
    short TWLG_BODO = 90;
    short TWLG_DOGRI = 91;
    short TWLG_GUJARATI = 92;
    short TWLG_HARYANVI = 93;
    short TWLG_HINDI = 94;
    short TWLG_KANNADA = 95;
    short TWLG_KASHMIRI = 96;
    short TWLG_MALAYALAM = 97;
    short TWLG_MARATHI = 98;
    short TWLG_MARWARI = 99;
    short TWLG_MEGHALAYAN = 100;
    short TWLG_MIZO = 101;
    short TWLG_NAGA = 102;
    short TWLG_ORISSI = 103;
    short TWLG_PUNJABI = 104;
    short TWLG_PUSHTU = 105;
    short TWLG_SERBIAN_CYRILLIC = 106;
    short TWLG_SIKKIMI = 107;
    short TWLG_SWEDISH_FINLAND = 108;
    short TWLG_TAMIL = 109;
    short TWLG_TELUGU = 110;
    short TWLG_TRIPURI = 111;
    short TWLG_URDU = 112;
    short TWLG_VIETNAMESE = 113;

    short TWCY_AFGHANISTAN = 1001;
    short TWCY_ALGERIA = 213;
    short TWCY_AMERICANSAMOA = 684;
    short TWCY_ANDORRA = 033;
    short TWCY_ANGOLA = 1002;
    short TWCY_ANGUILLA = 8090;
    short TWCY_ANTIGUA = 8091;
    short TWCY_ARGENTINA = 54;
    short TWCY_ARUBA = 297;
    short TWCY_ASCENSIONI = 247;
    short TWCY_AUSTRALIA = 61;
    short TWCY_AUSTRIA = 43;
    short TWCY_BAHAMAS = 8092;
    short TWCY_BAHRAIN = 973;
    short TWCY_BANGLADESH = 880;
    short TWCY_BARBADOS = 8093;
    short TWCY_BELGIUM = 32;
    short TWCY_BELIZE = 501;
    short TWCY_BENIN = 229;
    short TWCY_BERMUDA = 8094;
    short TWCY_BHUTAN = 1003;
    short TWCY_BOLIVIA = 591;
    short TWCY_BOTSWANA = 267;
    short TWCY_BRITAIN = 6;
    short TWCY_BRITVIRGINIS = 8095;
    short TWCY_BRAZIL = 55;
    short TWCY_BRUNEI = 673;
    short TWCY_BULGARIA = 359;
    short TWCY_BURKINAFASO = 1004;
    short TWCY_BURMA = 1005;
    short TWCY_BURUNDI = 1006;
    short TWCY_CAMAROON = 237;
    short TWCY_CANADA = 2;
    short TWCY_CAPEVERDEIS = 238;
    short TWCY_CAYMANIS = 8096;
    short TWCY_CENTRALAFREP = 1007;
    short TWCY_CHAD = 1008;
    short TWCY_CHILE = 56;
    short TWCY_CHINA = 86;
    short TWCY_CHRISTMASIS = 1009;
    short TWCY_COCOSIS = 1009;
    short TWCY_COLOMBIA = 57;
    short TWCY_COMOROS = 1010;
    short TWCY_CONGO = 1011;
    short TWCY_COOKIS = 1012;
    short TWCY_COSTARICA = 506;
    short TWCY_CUBA = 005;
    short TWCY_CYPRUS = 357;
    short TWCY_CZECHOSLOVAKIA = 42;
    short TWCY_DENMARK = 45;
    short TWCY_DJIBOUTI = 1013;
    short TWCY_DOMINICA = 8097;
    short TWCY_DOMINCANREP = 8098;
    short TWCY_EASTERIS = 1014;
    short TWCY_ECUADOR = 593;
    short TWCY_EGYPT = 20;
    short TWCY_ELSALVADOR = 503;
    short TWCY_EQGUINEA = 1015;
    short TWCY_ETHIOPIA = 251;
    short TWCY_FALKLANDIS = 1016;
    short TWCY_FAEROEIS = 298;
    short TWCY_FIJIISLANDS = 679;
    short TWCY_FINLAND = 358;
    short TWCY_FRANCE = 33;
    short TWCY_FRANTILLES = 596;
    short TWCY_FRGUIANA = 594;
    short TWCY_FRPOLYNEISA = 689;
    short TWCY_FUTANAIS = 1043;
    short TWCY_GABON = 241;
    short TWCY_GAMBIA = 220;
    short TWCY_GERMANY = 49;
    short TWCY_GHANA = 233;
    short TWCY_GIBRALTER = 350;
    short TWCY_GREECE = 30;
    short TWCY_GREENLAND = 299;
    short TWCY_GRENADA = 8099;
    short TWCY_GRENEDINES = 8015;
    short TWCY_GUADELOUPE = 590;
    short TWCY_GUAM = 671;
    short TWCY_GUANTANAMOBAY = 5399;
    short TWCY_GUATEMALA = 502;
    short TWCY_GUINEA = 224;
    short TWCY_GUINEABISSAU = 1017;
    short TWCY_GUYANA = 592;
    short TWCY_HAITI = 509;
    short TWCY_HONDURAS = 504;
    short TWCY_HONGKONG = 852;
    short TWCY_HUNGARY = 36;
    short TWCY_ICELAND = 354;
    short TWCY_INDIA = 91;
    short TWCY_INDONESIA = 62;
    short TWCY_IRAN = 98;
    short TWCY_IRAQ = 964;
    short TWCY_IRELAND = 353;
    short TWCY_ISRAEL = 972;
    short TWCY_ITALY = 39;
    short TWCY_IVORYCOAST = 225;
    short TWCY_JAMAICA = 8010;
    short TWCY_JAPAN = 81;
    short TWCY_JORDAN = 962;
    short TWCY_KENYA = 254;
    short TWCY_KIRIBATI = 1018;
    short TWCY_KOREA = 82;
    short TWCY_KUWAIT = 965;
    short TWCY_LAOS = 1019;
    short TWCY_LEBANON = 1020;
    short TWCY_LIBERIA = 231;
    short TWCY_LIBYA = 218;
    short TWCY_LIECHTENSTEIN = 41;
    short TWCY_LUXENBOURG = 352;
    short TWCY_MACAO = 853;
    short TWCY_MADAGASCAR = 1021;
    short TWCY_MALAWI = 265;
    short TWCY_MALAYSIA = 60;
    short TWCY_MALDIVES = 960;
    short TWCY_MALI = 1022;
    short TWCY_MALTA = 356;
    short TWCY_MARSHALLIS = 692;
    short TWCY_MAURITANIA = 1023;
    short TWCY_MAURITIUS = 230;
    short TWCY_MEXICO = 3;
    short TWCY_MICRONESIA = 691;
    short TWCY_MIQUELON = 508;
    short TWCY_MONACO = 33;
    short TWCY_MONGOLIA = 1024;
    short TWCY_MONTSERRAT = 8011;
    short TWCY_MOROCCO = 212;
    short TWCY_MOZAMBIQUE = 1025;
    short TWCY_NAMIBIA = 264;
    short TWCY_NAURU = 1026;
    short TWCY_NEPAL = 977;
    short TWCY_NETHERLANDS = 31;
    short TWCY_NETHANTILLES = 599;
    short TWCY_NEVIS = 8012;
    short TWCY_NEWCALEDONIA = 687;
    short TWCY_NEWZEALAND = 64;
    short TWCY_NICARAGUA = 505;
    short TWCY_NIGER = 227;
    short TWCY_NIGERIA = 234;
    short TWCY_NIUE = 1027;
    short TWCY_NORFOLKI = 1028;
    short TWCY_NORWAY = 47;
    short TWCY_OMAN = 968;
    short TWCY_PAKISTAN = 92;
    short TWCY_PALAU = 1029;
    short TWCY_PANAMA = 507;
    short TWCY_PARAGUAY = 595;
    short TWCY_PERU = 51;
    short TWCY_PHILLIPPINES = 63;
    short TWCY_PITCAIRNIS = 1030;
    short TWCY_PNEWGUINEA = 675;
    short TWCY_POLAND = 48;
    short TWCY_PORTUGAL = 351;
    short TWCY_QATAR = 974;
    short TWCY_REUNIONI = 1031;
    short TWCY_ROMANIA = 40;
    short TWCY_RWANDA = 250;
    short TWCY_SAIPAN = 670;
    short TWCY_SANMARINO = 39;
    short TWCY_SAOTOME = 1033;
    short TWCY_SAUDIARABIA = 966;
    short TWCY_SENEGAL = 221;
    short TWCY_SEYCHELLESIS = 1034;
    short TWCY_SIERRALEONE = 1035;
    short TWCY_SINGAPORE = 65;
    short TWCY_SOLOMONIS = 1036;
    short TWCY_SOMALI = 1037;
    short TWCY_SOUTHAFRICA = 27;
    short TWCY_SPAIN = 34;
    short TWCY_SRILANKA = 94;
    short TWCY_STHELENA = 1032;
    short TWCY_STKITTS = 8013;
    short TWCY_STLUCIA = 8014;
    short TWCY_STPIERRE = 508;
    short TWCY_STVINCENT = 8015;
    short TWCY_SUDAN = 1038;
    short TWCY_SURINAME = 597;
    short TWCY_SWAZILAND = 268;
    short TWCY_SWEDEN = 46;
    short TWCY_SWITZERLAND = 41;
    short TWCY_SYRIA = 1039;
    short TWCY_TAIWAN = 886;
    short TWCY_TANZANIA = 255;
    short TWCY_THAILAND = 66;
    short TWCY_TOBAGO = 8016;
    short TWCY_TOGO = 228;
    short TWCY_TONGAIS = 676;
    short TWCY_TRINIDAD = 8016;
    short TWCY_TUNISIA = 216;
    short TWCY_TURKEY = 90;
    short TWCY_TURKSCAICOS = 8017;
    short TWCY_TUVALU = 1040;
    short TWCY_UGANDA = 256;
    short TWCY_USSR = 7;
    short TWCY_UAEMIRATES = 971;
    short TWCY_UNITEDKINGDOM = 44;
    short TWCY_USA = 1;
    short TWCY_URUGUAY = 598;
    short TWCY_VANUATU = 1041;
    short TWCY_VATICANCITY = 39;
    short TWCY_VENEZUELA = 58;
    short TWCY_WAKE = 1042;
    short TWCY_WALLISIS = 1043;
    short TWCY_WESTERNSAHARA = 1044;
    short TWCY_WESTERNSAMOA = 1045;
    short TWCY_YEMEN = 1046;
    short TWCY_YUGOSLAVIA = 38;
    short TWCY_ZAIRE = 243;
    short TWCY_ZAMBIA = 260;
    short TWCY_ZIMBABWE = 263;
    short TWCY_ALBANIA = 355;
    short TWCY_ARMENIA = 374;
    short TWCY_AZERBAIJAN = 994;
    short TWCY_BELARUS = 375;
    short TWCY_BOSNIAHERZGO = 387;
    short TWCY_CAMBODIA = 855;
    short TWCY_CROATIA = 385;
    short TWCY_CZECHREPUBLIC = 420;
    short TWCY_DIEGOGARCIA = 246;
    short TWCY_ERITREA = 291;
    short TWCY_ESTONIA = 372;
    short TWCY_GEORGIA = 995;
    short TWCY_LATVIA = 371;
    short TWCY_LESOTHO = 266;
    short TWCY_LITHUANIA = 370;
    short TWCY_MACEDONIA = 389;
    short TWCY_MAYOTTEIS = 269;
    short TWCY_MOLDOVA = 373;
    short TWCY_MYANMAR = 95;
    short TWCY_NORTHKOREA = 850;
    short TWCY_PUERTORICO = 787;
    short TWCY_RUSSIA = 7;
    short TWCY_SERBIA = 381;
    short TWCY_SLOVAKIA = 421;
    short TWCY_SLOVENIA = 386;
    short TWCY_SOUTHKOREA = 82;
    short TWCY_UKRAINE = 380;
    short TWCY_USVIRGINIS = 340;
    short TWCY_VIETNAM = 84;

    /**
     * Data Groups *
     */
    int DG_CONTROL = 0x0001;
    int DG_IMAGE = 0x0002;
    int DG_AUDIO = 0x0004;

    /* More Data Functionality may be added in the future.
 * These are for items that need to be determined before DS is opened.
 * NOTE: Supported Functionality constants must be powers of 2 as they are
 *       used as bitflags when Application asks DSM to present a list of DSs.
 *       to support backward capability the App and DS will not use the fields
     */
    int DF_DSM2 = 0x10000000;
    int DF_APP2 = 0x20000000;

    int DF_DS2 = 0x40000000;

    int DG_MASK = 0xFFFF;

    /**
     * *************************************************************************
     * Return Codes and Condition Codes section *
     * *************************************************************************
     */
    int TWRC_CUSTOMBASE = 0x8000;

    int TWRC_SUCCESS = 0;
    int TWRC_FAILURE = 1;
    int TWRC_CHECKSTATUS = 2;
    int TWRC_CANCEL = 3;
    int TWRC_DSEVENT = 4;
    int TWRC_NOTDSEVENT = 5;
    int TWRC_XFERDONE = 6;
    int TWRC_ENDOFLIST = 7;
    int TWRC_INFONOTSUPPORTED = 8;
    int TWRC_DATANOTAVAILABLE = 9;
    int TWRC_BUSY = 10;
    int TWRC_SCANNERLOCKED = 11;

    /* Condition Codes: Application gets these by doing DG_CONTROL DAT_STATUS MSG_GET.  */
    int TWCC_CUSTOMBASE = 0x8000;

    int TWCC_SUCCESS = 0;
    int TWCC_BUMMER = 1;
    int TWCC_LOWMEMORY = 2;
    int TWCC_NODS = 3;
    int TWCC_MAXCONNECTIONS = 4;
    int TWCC_OPERATIONERROR = 5;
    int TWCC_BADCAP = 6;
    int TWCC_BADPROTOCOL = 9;
    int TWCC_BADVALUE = 10;
    int TWCC_SEQERROR = 11;
    int TWCC_BADDEST = 12;
    int TWCC_CAPUNSUPPORTED = 13;
    int TWCC_CAPBADOPERATION = 14;
    int TWCC_CAPSEQERROR = 15;
    int TWCC_DENIED = 16;
    int TWCC_FILEEXISTS = 17;
    int TWCC_FILENOTFOUND = 18;
    int TWCC_NOTEMPTY = 19;
    int TWCC_PAPERJAM = 20;
    int TWCC_PAPERDOUBLEFEED = 21;
    int TWCC_FILEWRITEERROR = 22;
    int TWCC_CHECKDEVICEONLINE = 23;
    int TWCC_INTERLOCK = 24;
    int TWCC_DAMAGEDCORNER = 25;
    int TWCC_FOCUSERROR = 26;
    int TWCC_DOCTOOLIGHT = 27;
    int TWCC_DOCTOODARK = 28;
    int TWCC_NOMEDIA = 29;

    /* bit patterns: for query the operation that are supported by the data source on a capability */
 /* Application gets these through DG_CONTROL/DAT_CAPABILITY/MSG_QUERYSUPPORT */
    int TWQC_GET = 0x0001;
    int TWQC_SET = 0x0002;
    int TWQC_GETDEFAULT = 0x0004;
    int TWQC_GETCURRENT = 0x0008;
    int TWQC_RESET = 0x0010;
    int TWQC_SETCONSTRAINT = 0x0020;
    int TWQC_CONSTRAINABLE = 0x0040;

    /**
     * **************************************************************************
     * Messages *
 ***************************************************************************
     */

    /* All message constants are unique.
 * Messages are grouped according to which DATs they are used with.*/
    short MSG_NULL = 0x0000;
    short MSG_CUSTOMBASE = -1;

    /* Generic messages may be used with any of several DATs.                   */
    short MSG_GET = 0x0001;
    short MSG_GETCURRENT = 0x0002;
    short MSG_GETDEFAULT = 0x0003;
    short MSG_GETFIRST = 0x0004;
    short MSG_GETNEXT = 0x0005;
    short MSG_SET = 0x0006;
    short MSG_RESET = 0x0007;
    short MSG_QUERYSUPPORT = 0x0008;
    short MSG_GETHELP = 0x0009;
    short MSG_GETLABEL = 0x000a;
    short MSG_GETLABELENUM = 0x000b;
    short MSG_SETCONSTRAINT = 0x000c;

    /* Messages used with DAT_NULL                                              */
    short MSG_XFERREADY = 0x0101;
    short MSG_CLOSEDSREQ = 0x0102;
    short MSG_CLOSEDSOK = 0x0103;
    short MSG_DEVICEEVENT = 0X0104;

    /* Messages used with a pointer to DAT_PARENT data                          */
    short MSG_OPENDSM = 0x0301;
    short MSG_CLOSEDSM = 0x0302;

    /* Messages used with a pointer to a DAT_IDENTITY structure                 */
    short MSG_OPENDS = 0x0401;
    short MSG_CLOSEDS = 0x0402;
    short MSG_USERSELECT = 0x0403;

    /* Messages used with a pointer to a DAT_USERINTERFACE structure            */
    short MSG_DISABLEDS = 0x0501;
    short MSG_ENABLEDS = 0x0502;
    short MSG_ENABLEDSUIONLY = 0x0503;

    /* Messages used with a pointer to a DAT_EVENT structure                    */
    short MSG_PROCESSEVENT = 0x0601;

    /* Messages used with a pointer to a DAT_PENDINGXFERS structure             */
    short MSG_ENDXFER = 0x0701;
    short MSG_STOPFEEDER = 0x0702;

    /* Messages used with a pointer to a DAT_FILESYSTEM structure               */
    short MSG_CHANGEDIRECTORY = 0x0801;
    short MSG_CREATEDIRECTORY = 0x0802;
    short MSG_DELETE = 0x0803;
    short MSG_FORMATMEDIA = 0x0804;
    short MSG_GETCLOSE = 0x0805;
    short MSG_GETFIRSTFILE = 0x0806;
    short MSG_GETINFO = 0x0807;
    short MSG_GETNEXTFILE = 0x0808;
    short MSG_RENAME = 0x0809;
    short MSG_COPY = 0x080A;
    short MSG_AUTOMATICCAPTUREDIRECTORY = 0x080B;

    /* Messages used with a pointer to a DAT_PASSTHRU structure                 */
    short MSG_PASSTHRU = 0x0901;

    /* used with DAT_CALLBACK */
    short MSG_REGISTER_CALLBACK = 0x0902;

    /* used with DAT_CAPABILITY */
    short MSG_RESETALL = 0x0A01;

    /**
     * **************************************************************************
     *                                                        *
     ***************************************************************************
     */
    short DAT_NULL = 0x0000;
    short DAT_CUSTOMBASE = -1;

    /* Data Argument Types for the DG_CONTROL Data Group. */
    short DAT_CAPABILITY = 0x0001;
    short DAT_EVENT = 0x0002;
    short DAT_IDENTITY = 0x0003;
    short DAT_PARENT = 0x0004;
    short DAT_PENDINGXFERS = 0x0005;
    short DAT_SETUPMEMXFER = 0x0006;
    short DAT_SETUPFILEXFER = 0x0007;
    short DAT_STATUS = 0x0008;
    short DAT_USERINTERFACE = 0x0009;
    short DAT_XFERGROUP = 0x000a;
    short DAT_CUSTOMDSDATA = 0x000c;
    short DAT_DEVICEEVENT = 0x000d;
    short DAT_FILESYSTEM = 0x000e;
    short DAT_PASSTHRU = 0x000f;
    short DAT_CALLBACK = 0x0010;
    short DAT_STATUSUTF8 = 0x0011;
    short DAT_CALLBACK2 = 0x0012;

    /* Data Argument Types for the DG_IMAGE Data Group. */
    short DAT_IMAGEINFO = 0x0101;
    short DAT_IMAGELAYOUT = 0x0102;
    short DAT_IMAGEMEMXFER = 0x0103;
    short DAT_IMAGENATIVEXFER = 0x0104;
    short DAT_IMAGEFILEXFER = 0x0105;
    short DAT_CIECOLOR = 0x0106;
    short DAT_GRAYRESPONSE = 0x0107;
    short DAT_RGBRESPONSE = 0x0108;
    short DAT_JPEGCOMPRESSION = 0x0109;
    short DAT_PALETTE8 = 0x010a;
    short DAT_EXTIMAGEINFO = 0x010b;
    short DAT_FILTER = 0x010c;

    /* Data Argument Types for the DG_AUDIO Data Group. */
    short DAT_AUDIOFILEXFER = 0x0201;
    short DAT_AUDIOINFO = 0x0202;
    short DAT_AUDIONATIVEXFER = 0x0203;

    /* misplaced */
    short DAT_ICCPROFILE = 0x0401;
    short DAT_IMAGEMEMFILEXFER = 0x0402;
    short DAT_ENTRYPOINT = 0x0403;

    /**
     * **************************************************************************
     * Generic Constants *
     * **************************************************************************
     */
    short TWON_ARRAY = 3;
    short TWON_ENUMERATION = 4;
    short TWON_ONEVALUE = 5;
    short TWON_RANGE = 6;

    short TWON_ICONID = 962;
    short TWON_DSMID = 461;
    short TWON_DSMCODEID = 63;

    short TWON_DONTCARE8 = 0xff;
    short TWON_DONTCARE16 = (short) 0xffff;
    int TWON_DONTCARE32 = 0xffffffff;

    /* Flags used in TW_MEMORY structure. */
    short TWMF_APPOWNS = 0x0001;
    short TWMF_DSMOWNS = 0x0002;
    short TWMF_DSOWNS = 0x0004;
    short TWMF_POINTER = 0x0008;
    short TWMF_HANDLE = 0x0010;

    short TWTY_INT8 = 0x0000;
    short TWTY_INT16 = 0x0001;
    short TWTY_INT32 = 0x0002;

    short TWTY_UINT8 = 0x0003;
    short TWTY_UINT16 = 0x0004;
    short TWTY_UINT32 = 0x0005;

    short TWTY_BOOL = 0x0006;

    short TWTY_FIX32 = 0x0007;

    short TWTY_FRAME = 0x0008;

    short TWTY_STR32 = 0x0009;
    short TWTY_STR64 = 0x000a;
    short TWTY_STR128 = 0x000b;
    short TWTY_STR255 = 0x000c;
    short TWTY_HANDLE = 0x000f;

    /**
     * **************************************************************************
     * Capability Constants *
 ***************************************************************************
     */

    /* CAP_ALARMS values */
    short TWAL_ALARM = 0;
    short TWAL_FEEDERERROR = 1;
    short TWAL_FEEDERWARNING = 2;
    short TWAL_BARCODE = 3;
    short TWAL_DOUBLEFEED = 4;
    short TWAL_JAM = 5;
    short TWAL_PATCHCODE = 6;
    short TWAL_POWER = 7;
    short TWAL_SKEW = 8;

    /* ICAP_AUTOSIZE values */
    short TWAS_NONE = 0;
    short TWAS_AUTO = 1;
    short TWAS_CURRENT = 2;

    /* TWEI_BARCODEROTATION values */
    short TWBCOR_ROT0 = 0;
    short TWBCOR_ROT90 = 1;
    short TWBCOR_ROT180 = 2;
    short TWBCOR_ROT270 = 3;
    short TWBCOR_ROTX = 4;

    /* ICAP_BARCODESEARCHMODE values */
    short TWBD_HORZ = 0;
    short TWBD_VERT = 1;
    short TWBD_HORZVERT = 2;
    short TWBD_VERTHORZ = 3;

    /* ICAP_BITORDER values */
    short TWBO_LSBFIRST = 0;
    short TWBO_MSBFIRST = 1;

    /* ICAP_AUTODISCARDBLANKPAGES values */
    short TWBP_DISABLE = -2;
    short TWBP_AUTO = -1;

    /* ICAP_BITDEPTHREDUCTION values */
    short TWBR_THRESHOLD = 0;
    short TWBR_HALFTONE = 1;
    short TWBR_CUSTHALFTONE = 2;
    short TWBR_DIFFUSION = 3;
    short TWBR_DYNAMICTHRESHOLD = 4;

    /* ICAP_SUPPORTEDBARCODETYPES and TWEI_BARCODETYPE values*/
    short TWBT_3OF9 = 0;
    short TWBT_2OF5INTERLEAVED = 1;
    short TWBT_2OF5NONINTERLEAVED = 2;
    short TWBT_CODE93 = 3;
    short TWBT_CODE128 = 4;
    short TWBT_UCC128 = 5;
    short TWBT_CODABAR = 6;
    short TWBT_UPCA = 7;
    short TWBT_UPCE = 8;
    short TWBT_EAN8 = 9;
    short TWBT_EAN13 = 10;
    short TWBT_POSTNET = 11;
    short TWBT_PDF417 = 12;
    short TWBT_2OF5INDUSTRIAL = 13;
    short TWBT_2OF5MATRIX = 14;
    short TWBT_2OF5DATALOGIC = 15;
    short TWBT_2OF5IATA = 16;
    short TWBT_3OF9FULLASCII = 17;
    short TWBT_CODABARWITHSTARTSTOP = 18;
    short TWBT_MAXICODE = 19;
    short TWBT_QRCODE = 20;

    /* ICAP_COMPRESSION values*/
    short TWCP_NONE = 0;
    short TWCP_PACKBITS = 1;
    short TWCP_GROUP31D = 2;
    short TWCP_GROUP31DEOL = 3;
    short TWCP_GROUP32D = 4;
    short TWCP_GROUP4 = 5;
    short TWCP_JPEG = 6;
    short TWCP_LZW = 7;
    short TWCP_JBIG = 8;
    short TWCP_PNG = 9;
    short TWCP_RLE4 = 10;
    short TWCP_RLE8 = 11;
    short TWCP_BITFIELDS = 12;
    short TWCP_ZIP = 13;
    short TWCP_JPEG2000 = 14;

    /* CAP_CAMERASIDE and TWEI_PAGESIDE values */
    short TWCS_BOTH = 0;
    short TWCS_TOP = 1;
    short TWCS_BOTTOM = 2;

    /* CAP_CLEARBUFFERS values */
    short TWCB_AUTO = 0;
    short TWCB_CLEAR = 1;
    short TWCB_NOCLEAR = 2;

    /* CAP_DEVICEEVENT values */
    short TWDE_CUSTOMEVENTS = -1;
    short TWDE_CHECKAUTOMATICCAPTURE = 0;
    short TWDE_CHECKBATTERY = 1;
    short TWDE_CHECKDEVICEONLINE = 2;
    short TWDE_CHECKFLASH = 3;
    short TWDE_CHECKPOWERSUPPLY = 4;
    short TWDE_CHECKRESOLUTION = 5;
    short TWDE_DEVICEADDED = 6;
    short TWDE_DEVICEOFFLINE = 7;
    short TWDE_DEVICEREADY = 8;
    short TWDE_DEVICEREMOVED = 9;
    short TWDE_IMAGECAPTURED = 10;
    short TWDE_IMAGEDELETED = 11;
    short TWDE_PAPERDOUBLEFEED = 12;
    short TWDE_PAPERJAM = 13;
    short TWDE_LAMPFAILURE = 14;
    short TWDE_POWERSAVE = 15;
    short TWDE_POWERSAVENOTIFY = 16;

    /* TW_PASSTHRU.Direction values. */
    short TWDR_GET = 1;
    short TWDR_SET = 2;

    /* TWEI_DESKEWSTATUS values. */
    short TWDSK_SUCCESS = 0;
    short TWDSK_REPORTONLY = 1;
    short TWDSK_FAIL = 2;
    short TWDSK_DISABLED = 3;

    /* CAP_DUPLEX values */
    short TWDX_NONE = 0;
    short TWDX_1PASSDUPLEX = 1;
    short TWDX_2PASSDUPLEX = 2;

    /* CAP_FEEDERALIGNMENT values */
    short TWFA_NONE = 0;
    short TWFA_LEFT = 1;
    short TWFA_CENTER = 2;
    short TWFA_RIGHT = 3;

    /* ICAP_FEEDERTYPE values*/
    short TWFE_GENERAL = 0;
    short TWFE_PHOTO = 1;

    /* ICAP_IMAGEFILEFORMAT values */
    short TWFF_TIFF = 0;
    short TWFF_PICT = 1;
    short TWFF_BMP = 2;
    short TWFF_XBM = 3;
    short TWFF_JFIF = 4;
    short TWFF_FPX = 5;
    short TWFF_TIFFMULTI = 6;
    short TWFF_PNG = 7;
    short TWFF_SPIFF = 8;
    short TWFF_EXIF = 9;
    short TWFF_PDF = 10;
    short TWFF_JP2 = 11;
    short TWFF_JPX = 13;
    short TWFF_DEJAVU = 14;
    short TWFF_PDFA = 15;
    short TWFF_PDFA2 = 16;

    /* ICAP_FLASHUSED2 values */
    short TWFL_NONE = 0;
    short TWFL_OFF = 1;
    short TWFL_ON = 2;
    short TWFL_AUTO = 3;
    short TWFL_REDEYE = 4;

    /* CAP_FEEDERORDER values */
    short TWFO_FIRSTPAGEFIRST = 0;
    short TWFO_LASTPAGEFIRST = 1;

    /* CAP_FEEDERPOCKET values*/
    short TWFP_POCKETERROR = 0;
    short TWFP_POCKET1 = 1;
    short TWFP_POCKET2 = 2;
    short TWFP_POCKET3 = 3;
    short TWFP_POCKET4 = 4;
    short TWFP_POCKET5 = 5;
    short TWFP_POCKET6 = 6;
    short TWFP_POCKET7 = 7;
    short TWFP_POCKET8 = 8;
    short TWFP_POCKET9 = 9;
    short TWFP_POCKET10 = 10;
    short TWFP_POCKET11 = 11;
    short TWFP_POCKET12 = 12;
    short TWFP_POCKET13 = 13;
    short TWFP_POCKET14 = 14;
    short TWFP_POCKET15 = 15;
    short TWFP_POCKET16 = 16;

    /* ICAP_FLIPROTATION values */
    short TWFR_BOOK = 0;
    short TWFR_FANFOLD = 1;

    /* ICAP_FILTER values */
    short TWFT_RED = 0;
    short TWFT_GREEN = 1;
    short TWFT_BLUE = 2;
    short TWFT_NONE = 3;
    short TWFT_WHITE = 4;
    short TWFT_CYAN = 5;
    short TWFT_MAGENTA = 6;
    short TWFT_YELLOW = 7;
    short TWFT_BLACK = 8;

    /* TW_FILESYSTEM.FileType values */
    short TWFY_CAMERA = 0;
    short TWFY_CAMERATOP = 1;
    short TWFY_CAMERABOTTOM = 2;
    short TWFY_CAMERAPREVIEW = 3;
    short TWFY_DOMAIN = 4;
    short TWFY_HOST = 5;
    short TWFY_DIRECTORY = 6;
    short TWFY_IMAGE = 7;
    short TWFY_UNKNOWN = 8;

    /* ICAP_ICCPROFILE values */
    short TWIC_NONE = 0;
    short TWIC_LINK = 1;
    short TWIC_EMBED = 2;

    /* ICAP_IMAGEFILTER values */
    short TWIF_NONE = 0;
    short TWIF_AUTO = 1;
    short TWIF_LOWPASS = 2;
    short TWIF_BANDPASS = 3;
    short TWIF_HIGHPASS = 4;
    short TWIF_TEXT = TWIF_BANDPASS;
    short TWIF_FINELINE = TWIF_HIGHPASS;
    /* ICAP_IMAGEMERGE values */
    short TWIM_NONE = 0;
    short TWIM_FRONTONTOP = 1;
    short TWIM_FRONTONBOTTOM = 2;
    short TWIM_FRONTONLEFT = 3;
    short TWIM_FRONTONRIGHT = 4;

    /* CAP_JOBCONTROL values  */
    short TWJC_NONE = 0;
    short TWJC_JSIC = 1;
    short TWJC_JSIS = 2;
    short TWJC_JSXC = 3;
    short TWJC_JSXS = 4;

    /* ICAP_JPEGQUALITY values */
    short TWJQ_UNKNOWN = -4;
    short TWJQ_LOW = -3;
    short TWJQ_MEDIUM = -2;
    short TWJQ_HIGH = -1;

    /* ICAP_LIGHTPATH values */
    short TWLP_REFLECTIVE = 0;
    short TWLP_TRANSMISSIVE = 1;

    /* ICAP_LIGHTSOURCE values */
    short TWLS_RED = 0;
    short TWLS_GREEN = 1;
    short TWLS_BLUE = 2;
    short TWLS_NONE = 3;
    short TWLS_WHITE = 4;
    short TWLS_UV = 5;
    short TWLS_IR = 6;

    /* TWEI_MAGTYPE values */
    short TWMD_MICR = 0;
    short TWMD_RAW = 1;
    short TWMD_INVALID = 2;

    /* ICAP_NOISEFILTER values */
    short TWNF_NONE = 0;
    short TWNF_AUTO = 1;
    short TWNF_LONEPIXEL = 2;
    short TWNF_MAJORITYRULE = 3;

    /* ICAP_ORIENTATION values */
    short TWOR_ROT0 = 0;
    short TWOR_ROT90 = 1;
    short TWOR_ROT180 = 2;
    short TWOR_ROT270 = 3;
    short TWOR_PORTRAIT = TWOR_ROT0;
    short TWOR_LANDSCAPE = TWOR_ROT270;
    short TWOR_AUTO = 4;
    short TWOR_AUTOTEXT = 5;
    short TWOR_AUTOPICTURE = 6;

    /* ICAP_OVERSCAN values */
    short TWOV_NONE = 0;
    short TWOV_AUTO = 1;
    short TWOV_TOPBOTTOM = 2;
    short TWOV_LEFTRIGHT = 3;
    short TWOV_ALL = 4;

    /* Palette types for TW_PALETTE8 */
    short TWPA_RGB = 0;
    short TWPA_GRAY = 1;
    short TWPA_CMY = 2;

    /* ICAP_PLANARCHUNKY values */
    short TWPC_CHUNKY = 0;
    short TWPC_PLANAR = 1;

    /* TWEI_PATCHCODE values*/
    short TWPCH_PATCH1 = 0;
    short TWPCH_PATCH2 = 1;
    short TWPCH_PATCH3 = 2;
    short TWPCH_PATCH4 = 3;
    short TWPCH_PATCH6 = 4;
    short TWPCH_PATCHT = 5;

    /* ICAP_PIXELFLAVOR values */
    short TWPF_CHOCOLATE = 0;
    short TWPF_VANILLA = 1;

    /* CAP_PRINTERMODE values */
    short TWPM_SINGLESTRING = 0;
    short TWPM_MULTISTRING = 1;
    short TWPM_COMPOUNDSTRING = 2;

    /* CAP_PRINTER values */
    short TWPR_IMPRINTERTOPBEFORE = 0;
    short TWPR_IMPRINTERTOPAFTER = 1;
    short TWPR_IMPRINTERBOTTOMBEFORE = 2;
    short TWPR_IMPRINTERBOTTOMAFTER = 3;
    short TWPR_ENDORSERTOPBEFORE = 4;
    short TWPR_ENDORSERTOPAFTER = 5;
    short TWPR_ENDORSERBOTTOMBEFORE = 6;
    short TWPR_ENDORSERBOTTOMAFTER = 7;

    /* CAP_POWERSUPPLY values */
    short TWPS_EXTERNAL = 0;
    short TWPS_BATTERY = 1;

    /* ICAP_PIXELTYPE values (PT_ means Pixel Type) */
    short TWPT_BW = 0;
    short TWPT_GRAY = 1;
    short TWPT_RGB = 2;
    short TWPT_PALETTE = 3;
    short TWPT_CMY = 4;
    short TWPT_CMYK = 5;
    short TWPT_YUV = 6;
    short TWPT_YUVK = 7;
    short TWPT_CIEXYZ = 8;
    short TWPT_LAB = 9;
    short TWPT_SRGB = 10;
    short TWPT_SCRGB = 11;
    short TWPT_INFRARED = 16;

    /* CAP_SEGMENTED values */
    short TWSG_NONE = 0;
    short TWSG_AUTO = 1;
    short TWSG_MANUAL = 2;

    /* ICAP_FILMTYPE values */
    short TWFM_POSITIVE = 0;
    short TWFM_NEGATIVE = 1;

    /* CAP_DOUBLEFEEDDETECTION */
    short TWDF_ULTRASONIC = 0;
    short TWDF_BYLENGTH = 1;
    short TWDF_INFRARED = 2;

    /* CAP_DOUBLEFEEDDETECTIONSENSITIVITY */
    short TWUS_LOW = 0;
    short TWUS_MEDIUM = 1;
    short TWUS_HIGH = 2;

    /* CAP_DOUBLEFEEDDETECTIONRESPONSE */
    short TWDP_STOP = 0;
    short TWDP_STOPANDWAIT = 1;
    short TWDP_SOUND = 2;
    short TWDP_DONOTIMPRINT = 3;

    /* ICAP_MIRROR values */
    short TWMR_NONE = 0;
    short TWMR_VERTICAL = 1;
    short TWMR_HORIZONTAL = 2;

    /* ICAP_JPEGSUBSAMPLING values */
    short TWJS_444YCBCR = 0;
    short TWJS_444RGB = 1;
    short TWJS_422 = 2;
    short TWJS_421 = 3;
    short TWJS_411 = 4;
    short TWJS_420 = 5;
    short TWJS_410 = 6;
    short TWJS_311 = 7;

    /* CAP_PAPERHANDLING values */
    short TWPH_NORMAL = 0;
    short TWPH_FRAGILE = 1;
    short TWPH_THICK = 2;
    short TWPH_TRIFOLD = 3;
    short TWPH_PHOTOGRAPH = 4;

    /* CAP_INDICATORSMODE values */
    short TWCI_INFO = 0;
    short TWCI_WARNING = 1;
    short TWCI_ERROR = 2;
    short TWCI_WARMUP = 3;

    /* ICAP_SUPPORTEDSIZES values (SS_ means Supported Sizes) */
    short TWSS_NONE = 0;
    short TWSS_A4 = 1;
    short TWSS_JISB5 = 2;
    short TWSS_USLETTER = 3;
    short TWSS_USLEGAL = 4;
    short TWSS_A5 = 5;
    short TWSS_ISOB4 = 6;
    short TWSS_ISOB6 = 7;
    short TWSS_USLEDGER = 9;
    short TWSS_USEXECUTIVE = 10;
    short TWSS_A3 = 11;
    short TWSS_ISOB3 = 12;
    short TWSS_A6 = 13;
    short TWSS_C4 = 14;
    short TWSS_C5 = 15;
    short TWSS_C6 = 16;
    short TWSS_4A0 = 17;
    short TWSS_2A0 = 18;
    short TWSS_A0 = 19;
    short TWSS_A1 = 20;
    short TWSS_A2 = 21;
    short TWSS_A7 = 22;
    short TWSS_A8 = 23;
    short TWSS_A9 = 24;
    short TWSS_A10 = 25;
    short TWSS_ISOB0 = 26;
    short TWSS_ISOB1 = 27;
    short TWSS_ISOB2 = 28;
    short TWSS_ISOB5 = 29;
    short TWSS_ISOB7 = 30;
    short TWSS_ISOB8 = 31;
    short TWSS_ISOB9 = 32;
    short TWSS_ISOB10 = 33;
    short TWSS_JISB0 = 34;
    short TWSS_JISB1 = 35;
    short TWSS_JISB2 = 36;
    short TWSS_JISB3 = 37;
    short TWSS_JISB4 = 38;
    short TWSS_JISB6 = 39;
    short TWSS_JISB7 = 40;
    short TWSS_JISB8 = 41;
    short TWSS_JISB9 = 42;
    short TWSS_JISB10 = 43;
    short TWSS_C0 = 44;
    short TWSS_C1 = 45;
    short TWSS_C2 = 46;
    short TWSS_C3 = 47;
    short TWSS_C7 = 48;
    short TWSS_C8 = 49;
    short TWSS_C9 = 50;
    short TWSS_C10 = 51;
    short TWSS_USSTATEMENT = 52;
    short TWSS_BUSINESSCARD = 53;
    short TWSS_MAXSIZE = 54;

    /* ICAP_XFERMECH values (SX_ means Setup XFer) */
    short TWSX_NATIVE = 0;
    short TWSX_FILE = 1;
    short TWSX_MEMORY = 2;
    short TWSX_MEMFILE = 4;

    /* ICAP_UNITS values (UN_ means UNits) */
    short TWUN_INCHES = 0;
    short TWUN_CENTIMETERS = 1;
    short TWUN_PICAS = 2;
    short TWUN_POINTS = 3;
    short TWUN_TWIPS = 4;
    short TWUN_PIXELS = 5;
    short TWUN_MILLIMETERS = 6;
}
