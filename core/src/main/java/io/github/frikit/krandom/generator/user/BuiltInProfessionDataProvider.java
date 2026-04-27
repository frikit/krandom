/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in profession provider for supported locales.
 */
final class BuiltInProfessionDataProvider implements ProfessionDataProvider {

    private static final int[] DEFAULT_WEIGHTS = new int[] {
        30, 28, 27, 24, 23,
        22, 21, 19, 18, 17,
        16, 16, 15, 14, 13,
        12, 12, 11, 11, 10,
        10, 9, 9, 8, 8
    };

    private final Locale   locale;
    private final String[] professions;

    BuiltInProfessionDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        this.professions = professionsFor(supportedLocale);
    }

    private static String[] professionsFor(SupportedLocale supportedLocale) {
        return switch (supportedLocale) {
            case EN_US, EN_CA, EN_IN, EN_ZA -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Lawyer", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case EN_GB, EN_IE -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Executive",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Solicitor", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case EN_AU, EN_NZ -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Barrister", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case FR_FR, FR_CA, FR_BE, FR_CH -> new String[] {
                "Ingénieur logiciel", "Professeur", "Infirmier", "Comptable", "Commercial",
                "Chef de projet", "Analyste de données", "Graphiste", "Électricien", "Mécanicien",
                "Avocat", "Médecin", "Pharmacien", "Chef cuisinier", "Architecte",
                "Ingénieur civil", "Spécialiste marketing", "Responsable RH", "Responsable opérations", "Chef de produit",
                "Designer UX", "Dentiste", "Ambulancier", "Analyste financier", "Agent support client"
            };
            case DE_DE, DE_AT, DE_CH -> new String[] {
                "Softwareentwickler", "Lehrer", "Krankenpfleger", "Buchhalter", "Vertriebsmitarbeiter",
                "Projektmanager", "Datenanalyst", "Grafikdesigner", "Elektriker", "Mechaniker",
                "Rechtsanwalt", "Arzt", "Apotheker", "Koch", "Architekt",
                "Bauingenieur", "Marketing-Spezialist", "Personalmanager", "Betriebsleiter", "Produktmanager",
                "UX-Designer", "Zahnarzt", "Sanitäter", "Finanzanalyst", "Kundensupport-Spezialist"
            };
            case JA_JP -> new String[] {
                "ソフトウェアエンジニア", "教師", "看護師", "会計士", "営業担当",
                "プロジェクトマネージャー", "データアナリスト", "グラフィックデザイナー", "電気技師", "整備士",
                "弁護士", "医師", "薬剤師", "料理人", "建築家",
                "土木技師", "マーケティング担当", "人事マネージャー", "運用マネージャー", "プロダクトマネージャー",
                "UXデザイナー", "歯科医師", "救急救命士", "財務アナリスト", "カスタマーサポート担当"
            };
            case ES_ES, ES_MX, ES_AR -> new String[] {
                "Ingeniero de software", "Profesor", "Enfermero", "Contable", "Representante de ventas",
                "Jefe de proyecto", "Analista de datos", "Diseñador gráfico", "Electricista", "Mecánico",
                "Abogado", "Médico", "Farmacéutico", "Chef", "Arquitecto",
                "Ingeniero civil", "Especialista en marketing", "Gerente de RR. HH.", "Gerente de operaciones", "Gerente de producto",
                "Diseñador UX", "Dentista", "Paramédico", "Analista financiero", "Especialista de soporte al cliente"
            };
            case IT_IT -> new String[] {
                "Ingegnere del software", "Insegnante", "Infermiere", "Contabile", "Rappresentante vendite",
                "Project manager", "Analista dati", "Grafico", "Elettricista", "Meccanico",
                "Avvocato", "Medico", "Farmacista", "Chef", "Architetto",
                "Ingegnere civile", "Specialista marketing", "Responsabile risorse umane", "Responsabile operativo", "Product manager",
                "Designer UX", "Dentista", "Paramedico", "Analista finanziario", "Specialista supporto clienti"
            };
            case PT_BR, PT_PT -> new String[] {
                "Engenheiro de software", "Professor", "Enfermeiro", "Contador", "Representante de vendas",
                "Gerente de projeto", "Analista de dados", "Designer gráfico", "Eletricista", "Mecânico",
                "Advogado", "Médico", "Farmacêutico", "Chef", "Arquiteto",
                "Engenheiro civil", "Especialista em marketing", "Gerente de RH", "Gerente de operações", "Gerente de produto",
                "Designer UX", "Dentista", "Paramédico", "Analista financeiro", "Especialista de suporte ao cliente"
            };
            case ZH_CN, ZH_TW -> new String[] {
                "软件工程师", "教师", "护士", "会计师", "销售代表",
                "项目经理", "数据分析师", "平面设计师", "电工", "机械师",
                "律师", "医生", "药剂师", "厨师", "建筑师",
                "土木工程师", "市场专员", "人力资源经理", "运营经理", "产品经理",
                "用户体验设计师", "牙医", "急救员", "财务分析师", "客户支持专员"
            };
            case NL_NL, NL_BE -> new String[] {
                "Softwareontwikkelaar", "Docent", "Verpleegkundige", "Boekhouder", "Verkoopmedewerker",
                "Projectmanager", "Data-analist", "Grafisch ontwerper", "Elektricien", "Monteur",
                "Advocaat", "Arts", "Apotheker", "Kok", "Architect",
                "Civiel ingenieur", "Marketeer", "HR-manager", "Operationeel manager", "Productmanager",
                "UX-designer", "Tandarts", "Ambulanceverpleegkundige", "Financieel analist", "Klantsupportspecialist"
            };
            case PL_PL -> new String[] {
                "Programista", "Nauczyciel", "Pielęgniarka", "Księgowy", "Przedstawiciel handlowy",
                "Kierownik projektu", "Analityk danych", "Grafik", "Elektryk", "Mechanik",
                "Prawnik", "Lekarz", "Farmaceuta", "Szef kuchni", "Architekt",
                "Inżynier budownictwa", "Specjalista ds. marketingu", "Menedżer HR", "Kierownik operacyjny", "Menedżer produktu",
                "Projektant UX", "Dentysta", "Ratownik medyczny", "Analityk finansowy", "Specjalista ds. obsługi klienta"
            };
            case CS_CZ -> new String[] {
                "Softwarový inženýr", "Učitel", "Zdravotní sestra", "Účetní", "Obchodní zástupce",
                "Projektový manažer", "Datový analytik", "Grafický designér", "Elektrikář", "Mechanik",
                "Právník", "Lékař", "Lékárník", "Šéfkuchař", "Architekt",
                "Stavební inženýr", "Marketingový specialista", "HR manažer", "Provozní manažer", "Produktový manažer",
                "UX designér", "Zubař", "Záchranář", "Finanční analytik", "Specialista zákaznické podpory"
            };
            case KO_KR -> new String[] {
                "소프트웨어 엔지니어", "교사", "간호사", "회계사", "영업 담당자",
                "프로젝트 매니저", "데이터 분석가", "그래픽 디자이너", "전기기사", "정비사",
                "변호사", "의사", "약사", "셰프", "건축가",
                "토목 엔지니어", "마케팅 전문가", "인사 관리자", "운영 관리자", "제품 관리자",
                "UX 디자이너", "치과의사", "응급구조사", "재무 분석가", "고객 지원 전문가"
            };
            case RU_RU -> new String[] {
                "Разработчик программного обеспечения", "Учитель", "Медсестра", "Бухгалтер", "Менеджер по продажам",
                "Руководитель проекта", "Аналитик данных", "Графический дизайнер", "Электрик", "Механик",
                "Юрист", "Врач", "Фармацевт", "Шеф-повар", "Архитектор",
                "Инженер-строитель", "Специалист по маркетингу", "HR-менеджер", "Операционный менеджер", "Продакт-менеджер",
                "UX-дизайнер", "Стоматолог", "Парамедик", "Финансовый аналитик", "Специалист поддержки клиентов"
            };
            case TR_TR -> new String[] {
                "Yazılım Mühendisi", "Öğretmen", "Hemşire", "Muhasebeci", "Satış Temsilcisi",
                "Proje Yöneticisi", "Veri Analisti", "Grafik Tasarımcı", "Elektrikçi", "Mekanik Teknisyeni",
                "Avukat", "Doktor", "Eczacı", "Şef", "Mimar",
                "İnşaat Mühendisi", "Pazarlama Uzmanı", "İK Müdürü", "Operasyon Müdürü", "Ürün Müdürü",
                "UX Tasarımcı", "Diş Hekimi", "Paramedik", "Finans Analisti", "Müşteri Destek Uzmanı"
            };
            case HI_IN -> new String[] {
                "सॉफ्टवेयर इंजीनियर", "शिक्षक", "नर्स", "लेखाकार", "बिक्री प्रतिनिधि",
                "परियोजना प्रबंधक", "डेटा विश्लेषक", "ग्राफिक डिज़ाइनर", "इलेक्ट्रीशियन", "मेकैनिक",
                "वकील", "डॉक्टर", "फार्मासिस्ट", "शेफ", "वास्तुकार",
                "सिविल इंजीनियर", "मार्केटिंग विशेषज्ञ", "एचआर प्रबंधक", "संचालन प्रबंधक", "उत्पाद प्रबंधक",
                "यूएक्स डिज़ाइनर", "दंत चिकित्सक", "पैरामेडिक", "वित्तीय विश्लेषक", "ग्राहक सहायता विशेषज्ञ"
            };
            case AR_SA -> new String[] {
                "مهندس برمجيات", "معلم", "ممرض", "محاسب", "مندوب مبيعات",
                "مدير مشروع", "محلل بيانات", "مصمم جرافيك", "كهربائي", "ميكانيكي",
                "محام", "طبيب", "صيدلي", "طاه", "مهندس معماري",
                "مهندس مدني", "أخصائي تسويق", "مدير موارد بشرية", "مدير عمليات", "مدير منتج",
                "مصمم تجربة مستخدم", "طبيب أسنان", "مسعف", "محلل مالي", "أخصائي دعم العملاء"
            };
            case SV_SE -> new String[] {
                "Mjukvaruingenjör", "Lärare", "Sjuksköterska", "Redovisningsekonom", "Säljare",
                "Projektledare", "Dataanalytiker", "Grafisk designer", "Elektriker", "Mekaniker",
                "Jurist", "Läkare", "Apotekare", "Kock", "Arkitekt",
                "Civilingenjör", "Marknadsföringsspecialist", "HR-chef", "Verksamhetschef", "Produktchef",
                "UX-designer", "Tandläkare", "Ambulanssjukvårdare", "Finansanalytiker", "Kundsupportspecialist"
            };
            case NB_NO -> new String[] {
                "Programvareutvikler", "Lærer", "Sykepleier", "Regnskapsfører", "Selger",
                "Prosjektleder", "Dataanalytiker", "Grafisk designer", "Elektriker", "Mekaniker",
                "Advokat", "Lege", "Farmasøyt", "Kokk", "Arkitekt",
                "Sivilingeniør", "Markedsspesialist", "HR-leder", "Driftsleder", "Produktsjef",
                "UX-designer", "Tannlege", "Ambulansearbeider", "Finansanalytiker", "Kundestøttespesialist"
            };
            case DA_DK -> new String[] {
                "Softwareingeniør", "Lærer", "Sygeplejerske", "Revisor", "Salgskonsulent",
                "Projektleder", "Dataanalytiker", "Grafisk designer", "Elektriker", "Mekaniker",
                "Advokat", "Læge", "Farmaceut", "Kok", "Arkitekt",
                "Civilingeniør", "Marketingspecialist", "HR-chef", "Driftschef", "Produktchef",
                "UX-designer", "Tandlæge", "Ambulanceredder", "Finansanalytiker", "Kundesupportmedarbejder"
            };
            case FI_FI -> new String[] {
                "Ohjelmistokehittäjä", "Opettaja", "Sairaanhoitaja", "Kirjanpitäjä", "Myyntiedustaja",
                "Projektipäällikkö", "Data-analyytikko", "Graafinen suunnittelija", "Sähköasentaja", "Mekaanikko",
                "Lakimies", "Lääkäri", "Farmaseutti", "Kokki", "Arkkitehti",
                "Rakennusinsinööri", "Markkinointiasiantuntija", "HR-päällikkö", "Toimintajohtaja", "Tuotepäällikkö",
                "UX-suunnittelija", "Hammaslääkäri", "Ensihoitaja", "Talousanalyytikko", "Asiakastukiasiantuntija"
            };
            case HU_HU -> new String[] {
                "Szoftverfejlesztő", "Tanár", "Ápoló", "Könyvelő", "Értékesítő",
                "Projektmenedzser", "Adatelemző", "Grafikus", "Villanyszerelő", "Szerelő",
                "Ügyvéd", "Orvos", "Gyógyszerész", "Séf", "Építész",
                "Építőmérnök", "Marketing szakértő", "HR vezető", "Üzemeltetési vezető", "Termékmenedzser",
                "UX tervező", "Fogorvos", "Mentős", "Pénzügyi elemző", "Ügyfélszolgálati munkatárs"
            };
            case RO_RO -> new String[] {
                "Inginer software", "Profesor", "Asistent medical", "Contabil", "Reprezentant vânzări",
                "Manager de proiect", "Analist de date", "Designer grafic", "Electrician", "Mecanic",
                "Avocat", "Medic", "Farmacist", "Bucătar-șef", "Arhitect",
                "Inginer constructor", "Specialist marketing", "Manager HR", "Director operațiuni", "Manager de produs",
                "Designer UX", "Dentist", "Paramedic", "Analist financiar", "Specialist suport clienți"
            };
            case SK_SK -> new String[] {
                "Softvérový inžinier", "Učiteľ", "Zdravotná sestra", "Účtovník", "Obchodný zástupca",
                "Projektový manažér", "Dátový analytik", "Grafický dizajnér", "Elektrikár", "Mechanik",
                "Právnik", "Lekár", "Lekárnik", "Šéfkuchár", "Architekt",
                "Stavebný inžinier", "Marketingový špecialista", "HR manažér", "Prevádzkový manažér", "Produktový manažér",
                "UX dizajnér", "Zubár", "Záchranár", "Finančný analytik", "Špecialista zákazníckej podpory"
            };
            case UK_UA -> new String[] {
                "Інженер-програміст", "Вчитель", "Медсестра", "Бухгалтер", "Менеджер з продажу",
                "Керівник проекту", "Аналітик даних", "Графічний дизайнер", "Електрик", "Механік",
                "Адвокат", "Лікар", "Фармацевт", "Шеф-кухар", "Архітектор",
                "Інженер-будівельник", "Маркетолог", "HR-менеджер", "Операційний менеджер", "Продакт-менеджер",
                "UX-дизайнер", "Стоматолог", "Парамедик", "Фінансовий аналітик", "Спеціаліст підтримки клієнтів"
            };
            case BG_BG -> new String[] {
                "Софтуерен инженер", "Учител", "Медицинска сестра", "Счетоводител", "Търговски представител",
                "Ръководител проект", "Анализатор на данни", "Графичен дизайнер", "Електротехник", "Механик",
                "Адвокат", "Лекар", "Фармацевт", "Готвач", "Архитект",
                "Строителен инженер", "Маркетинг специалист", "HR мениджър", "Оперативен мениджър", "Продуктов мениджър",
                "UX дизайнер", "Зъболекар", "Парамедик", "Финансов анализатор", "Специалист обслужване на клиенти"
            };
            case HR_HR -> new String[] {
                "Softverski inženjer", "Učitelj", "Medicinska sestra", "Računovođa", "Prodajni predstavnik",
                "Voditelj projekta", "Analitičar podataka", "Grafički dizajner", "Električar", "Mehaničar",
                "Odvjetnik", "Liječnik", "Ljekarnik", "Kuhar", "Arhitekt",
                "Građevinski inženjer", "Marketinški stručnjak", "HR voditelj", "Operativni voditelj", "Voditelj proizvoda",
                "UX dizajner", "Stomatolog", "Hitna pomoć", "Financijski analitičar", "Specijalist korisničke podrške"
            };
            case EL_GR -> new String[] {
                "Μηχανικός λογισμικού", "Εκπαιδευτικός", "Νοσηλευτής", "Λογιστής", "Εκπρόσωπος πωλήσεων",
                "Διαχειριστής έργου", "Αναλυτής δεδομένων", "Γραφίστας", "Ηλεκτρολόγος", "Μηχανικός",
                "Δικηγόρος", "Γιατρός", "Φαρμακοποιός", "Σεφ", "Αρχιτέκτονας",
                "Πολιτικός μηχανικός", "Ειδικός μάρκετινγκ", "Διευθυντής HR", "Διευθυντής λειτουργίας", "Διαχειριστής προϊόντος",
                "Σχεδιαστής UX", "Οδοντίατρος", "Παραϊατρικός", "Χρηματοοικονομικός αναλυτής", "Ειδικός υποστήριξης πελατών"
            };
            case TH_TH -> new String[] {
                "วิศวกรซอฟต์แวร์", "ครู", "พยาบาล", "นักบัญชี", "ตัวแทนขาย",
                "ผู้จัดการโครงการ", "นักวิเคราะห์ข้อมูล", "นักออกแบบกราฟิก", "ช่างไฟฟ้า", "ช่างเครื่อง",
                "ทนายความ", "แพทย์", "เภสัชกร", "เชฟ", "สถาปนิก",
                "วิศวกรโยธา", "ผู้เชี่ยวชาญด้านการตลาด", "ผู้จัดการฝ่ายทรัพยากรบุคคล", "ผู้จัดการฝ่ายปฏิบัติการ", "ผู้จัดการผลิตภัณฑ์",
                "นักออกแบบ UX", "ทันตแพทย์", "เจ้าหน้าที่กู้ชีพ", "นักวิเคราะห์การเงิน", "เจ้าหน้าที่สนับสนุนลูกค้า"
            };
            case VI_VN -> new String[] {
                "Kỹ sư phần mềm", "Giáo viên", "Y tá", "Kế toán", "Đại diện bán hàng",
                "Quản lý dự án", "Phân tích dữ liệu", "Nhà thiết kế đồ họa", "Thợ điện", "Thợ cơ khí",
                "Luật sư", "Bác sĩ", "Dược sĩ", "Đầu bếp", "Kiến trúc sư",
                "Kỹ sư xây dựng", "Chuyên viên marketing", "Quản lý nhân sự", "Quản lý vận hành", "Quản lý sản phẩm",
                "Nhà thiết kế UX", "Nha sĩ", "Nhân viên cấp cứu", "Phân tích tài chính", "Chuyên viên hỗ trợ khách hàng"
            };
            case ID_ID -> new String[] {
                "Insinyur perangkat lunak", "Guru", "Perawat", "Akuntan", "Perwakilan penjualan",
                "Manajer proyek", "Analis data", "Desainer grafis", "Teknisi listrik", "Mekanik",
                "Pengacara", "Dokter", "Apoteker", "Koki", "Arsitek",
                "Insinyur sipil", "Spesialis pemasaran", "Manajer SDM", "Manajer operasi", "Manajer produk",
                "Desainer UX", "Dokter gigi", "Paramedis", "Analis keuangan", "Spesialis layanan pelanggan"
            };
            case MS_MY -> new String[] {
                "Jurutera perisian", "Guru", "Jururawat", "Akauntan", "Wakil jualan",
                "Pengurus projek", "Penganalisis data", "Pereka grafik", "Juruteknik elektrik", "Mekanik",
                "Peguam", "Doktor", "Ahli farmasi", "Chef", "Arkitek",
                "Jurutera awam", "Pakar pemasaran", "Pengurus sumber manusia", "Pengurus operasi", "Pengurus produk",
                "Pereka UX", "Doktor gigi", "Paramedik", "Penganalisis kewangan", "Pakar sokongan pelanggan"
            };
            case HE_IL -> new String[] {
                "מהנדס תוכנה", "מורה", "אח", "רואה חשבון", "נציג מכירות",
                "מנהל פרויקט", "מנתח נתונים", "מעצב גרפי", "חשמלאי", "מכונאי",
                "עורך דין", "רופא", "רוקח", "שף", "אדריכל",
                "מהנדס אזרחי", "מומחה שיווק", "מנהל משאבי אנוש", "מנהל תפעול", "מנהל מוצר",
                "מעצב חוויית משתמש", "רופא שיניים", "חובש", "אנליסט פיננסי", "מומחה תמיכת לקוחות"
            };
            case CA_ES -> new String[] {
                "Enginyer de programari", "Professor", "Infermer", "Comptable", "Representant de vendes",
                "Cap de projecte", "Analista de dades", "Dissenyador gràfic", "Electricista", "Mecànic",
                "Advocat", "Metge", "Farmacèutic", "Xef", "Arquitecte",
                "Enginyer civil", "Especialista en màrqueting", "Gerent de recursos humans", "Gerent d'operacions", "Gerent de producte",
                "Dissenyador UX", "Dentista", "Paramèdic", "Analista financer", "Especialista de suport al client"
            };
        };
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getProfessions() {
        return professions.clone();
    }

    @Override
    public int[] getWeights() {
        return DEFAULT_WEIGHTS.clone();
    }
}
