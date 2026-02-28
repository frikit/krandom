/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.util.Locale;

/**
 * Built-in locale-specific profession data.
 *
 * <p>Each constant provides 25 profession values and rank weights for
 * {@link ProfessionGenerator#generateRanked()}.
 */
public enum LocaleProfessionData implements ProfessionDataProvider {

    EN_US(Locale.of("en", "US"),
            new String[]{
                    "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                    "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                    "Lawyer", "Doctor", "Pharmacist", "Chef", "Architect",
                    "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                    "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            }),

    EN_GB(Locale.of("en", "GB"),
            new String[]{
                    "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Executive",
                    "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                    "Solicitor", "Doctor", "Pharmacist", "Chef", "Architect",
                    "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                    "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            }),

    EN_AU(Locale.of("en", "AU"),
            new String[]{
                    "Software Engineer", "Teacher", "Nurse", "Accountant", "Sales Representative",
                    "Project Manager", "Data Analyst", "Graphic Designer", "Electrician", "Mechanic",
                    "Barrister", "Doctor", "Pharmacist", "Chef", "Architect",
                    "Civil Engineer", "Marketing Specialist", "HR Manager", "Operations Manager", "Product Manager",
                    "UX Designer", "Dentist", "Paramedic", "Financial Analyst", "Customer Support Specialist"
            }),

    FR_FR(Locale.of("fr", "FR"),
            new String[]{
                    "Ingénieur logiciel", "Professeur", "Infirmier", "Comptable", "Commercial",
                    "Chef de projet", "Analyste de données", "Graphiste", "Électricien", "Mécanicien",
                    "Avocat", "Médecin", "Pharmacien", "Chef cuisinier", "Architecte",
                    "Ingénieur civil", "Spécialiste marketing", "Responsable RH", "Responsable opérations", "Chef de produit",
                    "Designer UX", "Dentiste", "Ambulancier", "Analyste financier", "Agent support client"
            }),

    DE_DE(Locale.of("de", "DE"),
            new String[]{
                    "Softwareentwickler", "Lehrer", "Krankenpfleger", "Buchhalter", "Vertriebsmitarbeiter",
                    "Projektmanager", "Datenanalyst", "Grafikdesigner", "Elektriker", "Mechaniker",
                    "Rechtsanwalt", "Arzt", "Apotheker", "Koch", "Architekt",
                    "Bauingenieur", "Marketing-Spezialist", "Personalmanager", "Betriebsleiter", "Produktmanager",
                    "UX-Designer", "Zahnarzt", "Sanitäter", "Finanzanalyst", "Kundensupport-Spezialist"
            }),

    JA_JP(Locale.of("ja", "JP"),
            new String[]{
                    "ソフトウェアエンジニア", "教師", "看護師", "会計士", "営業担当",
                    "プロジェクトマネージャー", "データアナリスト", "グラフィックデザイナー", "電気技師", "整備士",
                    "弁護士", "医師", "薬剤師", "料理人", "建築家",
                    "土木技師", "マーケティング担当", "人事マネージャー", "運用マネージャー", "プロダクトマネージャー",
                    "UXデザイナー", "歯科医師", "救急救命士", "財務アナリスト", "カスタマーサポート担当"
            }),

    ES_ES(Locale.of("es", "ES"),
            new String[]{
                    "Ingeniero de software", "Profesor", "Enfermero", "Contable", "Representante de ventas",
                    "Jefe de proyecto", "Analista de datos", "Diseñador gráfico", "Electricista", "Mecánico",
                    "Abogado", "Médico", "Farmacéutico", "Chef", "Arquitecto",
                    "Ingeniero civil", "Especialista en marketing", "Gerente de RR. HH.", "Gerente de operaciones", "Gerente de producto",
                    "Diseñador UX", "Dentista", "Paramédico", "Analista financiero", "Especialista de soporte al cliente"
            }),

    IT_IT(Locale.of("it", "IT"),
            new String[]{
                    "Ingegnere del software", "Insegnante", "Infermiere", "Contabile", "Rappresentante vendite",
                    "Project manager", "Analista dati", "Grafico", "Elettricista", "Meccanico",
                    "Avvocato", "Medico", "Farmacista", "Chef", "Architetto",
                    "Ingegnere civile", "Specialista marketing", "Responsabile risorse umane", "Responsabile operativo", "Product manager",
                    "Designer UX", "Dentista", "Paramedico", "Analista finanziario", "Specialista supporto clienti"
            }),

    PT_BR(Locale.of("pt", "BR"),
            new String[]{
                    "Engenheiro de software", "Professor", "Enfermeiro", "Contador", "Representante de vendas",
                    "Gerente de projeto", "Analista de dados", "Designer gráfico", "Eletricista", "Mecânico",
                    "Advogado", "Médico", "Farmacêutico", "Chef", "Arquiteto",
                    "Engenheiro civil", "Especialista em marketing", "Gerente de RH", "Gerente de operações", "Gerente de produto",
                    "Designer UX", "Dentista", "Paramédico", "Analista financeiro", "Especialista de suporte ao cliente"
            }),

    ZH_CN(Locale.of("zh", "CN"),
            new String[]{
                    "软件工程师", "教师", "护士", "会计师", "销售代表",
                    "项目经理", "数据分析师", "平面设计师", "电工", "机械师",
                    "律师", "医生", "药剂师", "厨师", "建筑师",
                    "土木工程师", "市场专员", "人力资源经理", "运营经理", "产品经理",
                    "用户体验设计师", "牙医", "急救员", "财务分析师", "客户支持专员"
            });

    private static final int[] DEFAULT_WEIGHTS = new int[]{
            30, 28, 27, 24, 23,
            22, 21, 19, 18, 17,
            16, 16, 15, 14, 13,
            12, 12, 11, 11, 10,
            10, 9, 9, 8, 8
    };

    private final Locale locale;
    private final String[] professions;

    LocaleProfessionData(Locale locale, String[] professions) {
        this.locale = locale;
        this.professions = professions;
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
