package io.debezium.relational;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class SystemVariables {
    private final Map<Scope, ConcurrentMap<String, String>> systemVariables = new ConcurrentHashMap();

    public SystemVariables() {
        this.systemVariables.put(DefaultScope.DEFAULT_SCOPE, new ConcurrentHashMap());
    }

    public SystemVariables(Scope[] scopes) {
        Scope[] var2 = scopes;
        int var3 = scopes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Scope scope = var2[var4];
            this.systemVariables.put(scope, new ConcurrentHashMap());
        }

    }

    public SystemVariables(List<Scope> scopes) {
        Iterator var2 = scopes.iterator();

        while (var2.hasNext()) {
            Scope scope = (Scope) var2.next();
            this.systemVariables.put(scope, new ConcurrentHashMap());
        }

    }

    public SystemVariables setVariable(Scope scope, String name, String value) {
        name = this.variableName(name);
        if (value != null) {
            this.forScope(scope).put(name, value);
        } else {
            this.forScope(scope).remove(name);
        }

        return this;
    }

    public String getVariable(String name, Scope scope) {
        name = this.variableName(name);
        return (String) this.forScope(scope).get(name);
    }

    public String getVariable(String name) {
        List<ConcurrentMap<String, String>> orderedSystemVariablesByPriority = this.getOrderedSystemVariablesByScopePriority();
        name = this.variableName(name);
        Iterator var3 = orderedSystemVariablesByPriority.iterator();

        String variableName;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            ConcurrentMap<String, String> variablesByScope = (ConcurrentMap) var3.next();
            variableName = (String) variablesByScope.get(name);
        } while (variableName == null);

        return variableName;
    }

    private List<ConcurrentMap<String, String>> getOrderedSystemVariablesByScopePriority() {
        return (List) this.systemVariables.entrySet().stream().sorted(Comparator.comparingInt((entry) -> {
            return ((Scope) entry.getKey()).priority();
        })).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    private String variableName(String name) {
        return name.toLowerCase();
    }

    protected ConcurrentMap<String, String> forScope(Scope scope) {
        if (scope != null) {
            return (ConcurrentMap) this.systemVariables.computeIfAbsent(scope, (entities) -> {
                return new ConcurrentHashMap();
            });
        } else {
            List<ConcurrentMap<String, String>> orderedSystemVariablesByScopePriority = this.getOrderedSystemVariablesByScopePriority();
            return orderedSystemVariablesByScopePriority.isEmpty() ? null : (ConcurrentMap) orderedSystemVariablesByScopePriority.get(0);
        }
    }

    public static enum DefaultScope implements Scope {
        DEFAULT_SCOPE(100);

        private int priority;

        private DefaultScope(int priority) {
            this.priority = priority;
        }

        public int priority() {
            return this.priority;
        }

        // $FF: synthetic method
        private static DefaultScope[] $values() {
            return new DefaultScope[]{DEFAULT_SCOPE};
        }
    }

    public interface Scope {
        int priority();
    }
}
