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
            case EN_US -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Lawyer", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case EN_GB -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Executive",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Solicitor", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case EN_AU -> new String[] {
                "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                "Barrister", "Doctor", "Pharmacist", "Chef", "Architect",
                "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            };
            case FR_FR -> new String[] {
                "Ingénieur logiciel", "Professeur", "Infirmier", "Comptable", "Commercial",
                "Chef de projet", "Analyste de données", "Graphiste", "Électricien", "Mécanicien",
                "Avocat", "Médecin", "Pharmacien", "Chef cuisinier", "Architecte",
                "Ingénieur civil", "Spécialiste marketing", "Responsable RH", "Responsable opérations", "Chef de produit",
                "Designer UX", "Dentiste", "Ambulancier", "Analyste financier", "Agent support client"
            };
            case DE_DE -> new String[] {
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
            case ES_ES -> new String[] {
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
            case PT_BR -> new String[] {
                "Engenheiro de software", "Professor", "Enfermeiro", "Contador", "Representante de vendas",
                "Gerente de projeto", "Analista de dados", "Designer gráfico", "Eletricista", "Mecânico",
                "Advogado", "Médico", "Farmacêutico", "Chef", "Arquiteto",
                "Engenheiro civil", "Especialista em marketing", "Gerente de RH", "Gerente de operações", "Gerente de produto",
                "Designer UX", "Dentista", "Paramédico", "Analista financeiro", "Especialista de suporte ao cliente"
            };
            case ZH_CN -> new String[] {
                "软件工程师", "教师", "护士", "会计师", "销售代表",
                "项目经理", "数据分析师", "平面设计师", "电工", "机械师",
                "律师", "医生", "药剂师", "厨师", "建筑师",
                "土木工程师", "市场专员", "人力资源经理", "运营经理", "产品经理",
                "用户体验设计师", "牙医", "急救员", "财务分析师", "客户支持专员"
            };
            case NL_NL -> new String[] {
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
